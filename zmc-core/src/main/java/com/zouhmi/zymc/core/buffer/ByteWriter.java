package com.zouhmi.zymc.core.buffer;

@FunctionalInterface
public interface ByteWriter {
    void writeByte(int value) throws java.io.IOException;
}
