package com.zouhmi.zymc.core.buffer;

import java.util.Arrays;

public final class Bytes implements BufferReader, BufferWriter {
    private byte[] data;
    private int readPos;
    private int writePos;

    public Bytes() {
        this(256);
    }

    public Bytes(int capacity) {
        this.data = new byte[capacity];
        this.readPos = 0;
        this.writePos = 0;
    }

    public int capacity() {
        return data.length;
    }

    public Bytes ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            data = Arrays.copyOf(data, Math.max(data.length * 2, minCapacity));
        }
        return this;
    }

    public int readPosition() {
        return readPos;
    }

    public void setReadPosition(int readPos) {
        if (readPos < 0 || readPos > writePos) {
            throw new IndexOutOfBoundsException("readPos=" + readPos + " writePos=" + writePos);
        }
        this.readPos = readPos;
    }

    public int writePosition() {
        return writePos;
    }

    public void setWritePosition(int writePos) {
        if (writePos < readPos || writePos > data.length) {
            throw new IndexOutOfBoundsException("writePos=" + writePos + " readPos=" + readPos);
        }
        this.writePos = writePos;
    }

    public int readableBytes() {
        return writePos - readPos;
    }

    public int writableBytes() {
        return data.length - writePos;
    }

    @Override
    public int readByte() {
        if (readPos >= writePos) throw new IllegalStateException("No byte to read");
        return data[readPos++] & 0xFF;
    }

    @Override
    public int readUnsignedByte() {
        return readByte();
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        if (readPos + length > writePos) {
            throw new IllegalStateException("Not enough readable bytes: need " + length + " have " + (writePos - readPos));
        }
        System.arraycopy(data, readPos, dst, offset, length);
        readPos += length;
    }

    @Override
    public void skip(int bytes) {
        if (readPos + bytes > writePos) {
            throw new IllegalStateException("Cannot skip " + bytes + " bytes; only " + (writePos - readPos) + " available");
        }
        readPos += bytes;
    }

    @Override
    public void writeByte(int value) {
        ensureCapacity(writePos + 1);
        data[writePos++] = (byte) (value & 0xFF);
    }

    @Override
    public void writeBytes(byte[] src, int offset, int length) {
        ensureCapacity(writePos + length);
        System.arraycopy(src, offset, data, writePos, length);
        writePos += length;
    }

    @Override
    public void writeBytes(byte[] src) {
        writeBytes(src, 0, src.length);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(data, writePos);
    }

    public void clear() {
        readPos = 0;
        writePos = 0;
    }
}
