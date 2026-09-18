package com.zouhmi.zymc.core.buffer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Strings {
    private static final int MAX_STRING_BYTES = 32767;

    private Strings() {}

    public static String readString(ByteReader in) throws IOException {
        int byteCount = VarInts.readVarInt(in);
        if (byteCount < 0 || byteCount > MAX_STRING_BYTES) {
            throw new IOException("Invalid string byte count: " + byteCount);
        }
        byte[] bytes = new byte[byteCount];
        readFully(in, bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteWriter out, CharSequence value) throws IOException {
        byte[] bytes = value.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_STRING_BYTES) {
            throw new IOException("String exceeds maximum length: " + bytes.length);
        }
        VarInts.writeVarInt(out, bytes.length);
        writeFully(out, bytes);
    }

    private static void readFully(ByteReader in, byte[] dst) throws IOException {
        for (int i = 0; i < dst.length; i++) {
            int b = in.readByte();
            if (b == -1) throw new IOException("Unexpected end of stream reading string");
            dst[i] = (byte) b;
        }
    }

    private static void writeFully(ByteWriter out, byte[] src) throws IOException {
        for (byte b : src) {
            out.writeByte(b);
        }
    }
}
