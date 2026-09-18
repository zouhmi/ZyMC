package com.zouhmi.zymc.network.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NettyCipherDecoder extends ByteToMessageDecoder {
    private final Cipher cipher;

    public NettyCipherDecoder(byte[] sharedSecret) {
        try {
            this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
            this.cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AES/CFB8 cipher for decoding", e);
        }
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
