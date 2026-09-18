package com.zouhmi.zymc.core.buffer;

@FunctionalInterface
public interface ByteReader {
    int readByte() throws java.io.IOException;
}
