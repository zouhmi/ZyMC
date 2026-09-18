package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public final class LoginKeyC2SCodec implements PacketCodec<LoginKeyC2S> {

    @Override
    public void encode(LoginKeyC2S packet, ByteBuf buf) {
        try {
            writeBytes(buf, packet.encryptedSecretKey());
            writeBytes(buf, packet.nonce());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginKeyC2S decode(ByteBuf buf) {
        try {
            byte[] encryptedSecretKey = readBytes(buf);
            byte[] nonce = readBytes(buf);
            return new LoginKeyC2S(encryptedSecretKey, nonce);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeBytes(ByteBuf buf, byte[] bytes) throws IOException {
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    private static byte[] readBytes(ByteBuf buf) throws IOException {
        int length = readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }

    private static void writeVarInt(ByteBuf out, int value) throws IOException {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }

    private static int readVarInt(ByteBuf in) throws IOException {
        int out = 0;
        int bytes = 0;
        int b;
        while (bytes < 5) {
            if (!in.isReadable()) throw new IOException("Unexpected end of stream reading VarInt");
            b = in.readByte() & 0xFF;
            out |= (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }
}
