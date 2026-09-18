package com.zouhmi.zymc.network.protocol;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public final class ByteBufVarInts {

    private ByteBufVarInts() {}

    public static int readVarInt(ByteBuf in) {
        int out = 0;
        int bytes = 0;
        int b;
        while (bytes < 5) {
            if (!in.isReadable()) throw new IllegalArgumentException("Unexpected end of stream reading VarInt");
            b = in.readByte() & 0xFF;
            out |= (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

    public static void writeVarInt(ByteBuf out, int value) {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }

    public static long readVarLong(ByteBuf in) {
        long out = 0;
        int bytes = 0;
        int b;
        while (bytes < 10) {
            if (!in.isReadable()) throw new IllegalArgumentException("Unexpected end of stream reading VarLong");
            b = in.readByte() & 0xFF;
            out |= (long) (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

    public static void writeVarLong(ByteBuf out, long value) {
        int part;
        do {
            part = (int) (value & 0x7F);
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }

    public static int varIntSize(int value) {
        int size = 1;
        value >>>= 7;
        while (value != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }

    public static void writeString(ByteBuf out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.writeBytes(bytes);
    }

    public static String readString(ByteBuf in) {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeBytes(ByteBuf out, byte[] bytes) {
        writeVarInt(out, bytes.length);
        out.writeBytes(bytes);
    }

    public static byte[] readBytes(ByteBuf in) {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return bytes;
    }

    public static void writeBoolean(ByteBuf out, boolean value) {
        out.writeByte(value ? 1 : 0);
    }

    public static boolean readBoolean(ByteBuf in) {
        return in.readByte() != 0;
    }
}
