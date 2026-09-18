package com.zouhmi.zymc.core.buffer;

public interface BufferWriter extends ByteWriter {
    @Override
    void writeByte(int value) throws java.io.IOException;
    void writeBytes(byte[] src, int offset, int length) throws java.io.IOException;
    void writeBytes(byte[] src) throws java.io.IOException;
}
