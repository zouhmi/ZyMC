package com.zouhmi.zymc.core.buffer;

public interface BufferReader extends ByteReader {
    @Override
    int readByte() throws java.io.IOException;
    int readUnsignedByte() throws java.io.IOException;
    void readBytes(byte[] dst, int offset, int length) throws java.io.IOException;
    void skip(int bytes) throws java.io.IOException;
}
