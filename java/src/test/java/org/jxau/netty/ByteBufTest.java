package org.jxau.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class ByteBufTest {
    public static void main(String[] args) {
        ByteBufAllocator.DEFAULT.buffer(122);
    }
}
