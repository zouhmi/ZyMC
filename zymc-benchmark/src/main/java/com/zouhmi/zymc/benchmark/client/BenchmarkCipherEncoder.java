package com.zouhmi.zymc.benchmark.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;

public final class BenchmarkCipherEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Cipher cipher;

    public BenchmarkCipherEncoder(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int readable = msg.readableBytes();
        byte[] input = new byte[readable];
        msg.readBytes(input);
        byte[] encrypted = cipher.update(input);
        out.writeBytes(encrypted);
    }
}
