package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.core.buffer.Strings;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public final class LoginHelloS2CCodec implements PacketCodec<LoginHelloS2C> {

    @Override
    public void encode(LoginHelloS2C packet, ByteBuf buf) {
        try {
            Strings.writeString(buf::writeByte, packet.serverId());
            writeBytes(buf, packet.publicKeyDer());
            writeBytes(buf, packet.nonce());
            writeBoolean(buf, packet.needsAuthentication());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginHelloS2C decode(ByteBuf buf) {
        try {
            String serverId = Strings.readString(buf::readByte);
            byte[] publicKeyDer = readBytes(buf);
            byte[] nonce = readBytes(buf);
            boolean needsAuthentication = readBoolean(buf);
            return new LoginHelloS2C(serverId, publicKeyDer, nonce, needsAuthentication);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeBytes(ByteBuf buf, byte[] bytes) throws IOException {
        LocalVarInts.writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    private static byte[] readBytes(ByteBuf buf) throws IOException {
        int length = LocalVarInts.readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }

    private static void writeBoolean(ByteBuf buf, boolean value) {
        buf.writeByte(value ? 0x01 : 0x00);
    }

    private static boolean readBoolean(ByteBuf buf) {
        return buf.readByte() != 0x00;
    }

    private static final class LocalVarInts {
        public static int readVarInt(ByteBuf in) throws IOException {
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

        public static void writeVarInt(ByteBuf out, int value) throws IOException {
            int part;
            do {
                part = value & 0x7F;
                value >>>= 7;
                if (value != 0) part |= 0x80;
                out.writeByte(part);
            } while (value != 0);
        }
    }
}
