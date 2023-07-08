package cn.jxau.netty.pipeline;

import cn.jxau.netty.config.NettyServerConfig;
import cn.jxau.netty.exception.RemoteException;
import cn.jxau.netty.utils.NettyUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class NettyServerDemo {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    public static void main(String[] args) throws InterruptedException {
        new ServerBootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                    ch.pipeline()
                        .addLast("h1", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(1);
                                log.debug("1");
                                // 反序列化为字符串，并传递给下一个ChannelInboundHandler进行继续加工
                                ByteBuf buf = (ByteBuf) msg;
                                String name = buf.toString(Charset.defaultCharset());
                                super.channelRead(ctx, name);
                            }
                        })
                        .addLast("h2", new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
                                        log.debug("2");
                                        Student student = new Student(name.toString());

                                        // 向下传递，以下方法二选一，否则责任链会断开
                                        // 向下继续寻找入栈处理器handler，中间有出栈处理器handler，会跳过，继续向下寻找入栈处理器handler
                                        super.channelRead(ctx, student);
                                        //ctx.fireChannelRead(student);
                                    }
                                }
                        )
                        .addLast("h3", new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("3, 结果{}， class：{}", msg, msg.getClass());

                                        // 将结果写出，从而触发出站处理器的执行
                                        // ch.writeAndFlush: 从tail向前找出栈处理器handler
                                        ch.writeAndFlush(ctx.alloc().buffer().writeBytes("Server...".getBytes()));

                                        // ctx.writeAndFlush: 从当前handler(即h3)，向前找出栈处理器handler，这里会找不到，因为h3前面没有出栈处理器handler
                                        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("Server...".getBytes()));
                                    }
                                }
                        )
                        .addLast("h4", new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        })
                        .addLast("h5", new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        })
                        .addLast("h6", new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });
                }
            }).bind(12346);
    }


    static class Student {
        private String name;

        public Student(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}

