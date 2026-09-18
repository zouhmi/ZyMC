package com.zouhmi.zymc.network.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NettyCipherEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Cipher cipher;

    public NettyCipherEncoder(byte[] sharedSecret) {
        try {
            this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
            this.cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AES/CFB8 cipher for encoding", e);
        }
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
