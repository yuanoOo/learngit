[2023-05-18 13:57:41.440]Container exited with a non-zero exit code 1. Error file: prelaunch.err.
Last 4096 bytes of prelaunch.err :
Last 4096 bytes of stderr :
ansferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,903 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,920 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,936 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,952 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,967 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,983 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:40,999 WARN util.Utils: Service 'org.apache.spark.network.netty.NettyBlockTransferService' could not bind on a random free port. You may check whether configuring an appropriate binding address.
2023-05-18 13:57:41,013 ERROR executor.CoarseGrainedExecutorBackend: Executor self-exiting due to : Unable to create executor due to Address already in use: Service 'org.apache.spark.network.netty.NettyBlockTransferService' failed after 16 retries (on a random free port)! Consider explicitly setting the appropriate binding address for the service 'org.apache.spark.network.netty.NettyBlockTransferService' (for example spark.driver.bindAddress for SparkDriver) to the correct binding address.
java.net.BindException: Address already in use: Service 'org.apache.spark.network.netty.NettyBlockTransferService' failed after 16 retries (on a random free port)! Consider explicitly setting the appropriate binding address for the service 'org.apache.spark.network.netty.NettyBlockTransferService' (for example spark.driver.bindAddress for SparkDriver) to the correct binding address.
	at sun.nio.ch.Net.bind0(Native Method)
	at sun.nio.ch.Net.bind(Net.java:433)
	at sun.nio.ch.Net.bind(Net.java:425)
	at sun.nio.ch.ServerSocketChannelImpl.bind(ServerSocketChannelImpl.java:223)
	at io.netty.channel.socket.nio.NioServerSocketChannel.doBind(NioServerSocketChannel.java:134)
	at io.netty.channel.AbstractChannel$AbstractUnsafe.bind(AbstractChannel.java:550)
	at io.netty.channel.DefaultChannelPipeline$HeadContext.bind(DefaultChannelPipeline.java:1334)
	at io.netty.channel.AbstractChannelHandlerContext.invokeBind(AbstractChannelHandlerContext.java:506)
	at io.netty.channel.AbstractChannelHandlerContext.bind(AbstractChannelHandlerContext.java:491)
	at io.netty.channel.DefaultChannelPipeline.bind(DefaultChannelPipeline.java:973)
	at io.netty.channel.AbstractChannel.bind(AbstractChannel.java:248)
	at io.netty.bootstrap.AbstractBootstrap$2.run(AbstractBootstrap.java:356)
	at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:164)
	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:472)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:500)
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(Thread.java:748)
2023-05-18 13:57:41,022 INFO storage.DiskBlockManager: Shutdown hook called
2023-05-18 13:57:41,023 INFO util.ShutdownHookManager: Shutdown hook called


http://bcxw.net/article/797.html

 netstat -anp | grep 54889
