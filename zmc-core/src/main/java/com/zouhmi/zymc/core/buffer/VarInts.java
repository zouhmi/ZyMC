package com.zouhmi.zymc.core.buffer;

import java.io.IOException;

public final class VarInts {
    private VarInts() {}

    public static int readVarInt(ByteReader in) throws IOException {
        int out = 0;
        int bytes = 0;
        int b;
        while (bytes < 5) {
            b = in.readByte();
            if (b == -1) throw new IOException("Unexpected end of stream while reading VarInt");
            out |= (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

    public static void writeVarInt(ByteWriter out, int value) throws IOException {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }

    public static long readVarLong(ByteReader in) throws IOException {
        long out = 0;
        int bytes = 0;
        int b;
        while (bytes < 10) {
            b = in.readByte();
            if (b == -1) throw new IOException("Unexpected end of stream while reading VarLong");
            out |= (long) (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

    public static void writeVarLong(ByteWriter out, long value) throws IOException {
        int part;
        do {
            part = (int) (value & 0x7F);
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }

    public static int getVarIntSize(int value) {
        int size = 1;
        value >>>= 7;
        while (value != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }
}
