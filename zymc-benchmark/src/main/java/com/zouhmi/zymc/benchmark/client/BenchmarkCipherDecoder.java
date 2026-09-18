package com.zouhmi.zymc.benchmark.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

public final class BenchmarkCipherDecoder extends ByteToMessageDecoder {
    private final Cipher cipher;

    public BenchmarkCipherDecoder(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readable = in.readableBytes();
        byte[] input = new byte[readable];
        in.readBytes(input);
        byte[] decrypted = cipher.update(input);
        out.add(Unpooled.wrappedBuffer(decrypted));
    }
}
