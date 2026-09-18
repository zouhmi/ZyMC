package com.zouhmi.zymc.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkSection {
    private short blockCount;
    private final List<Integer> palette;
    private final Map<Integer, Integer> paletteIdToStateId;
    private final Map<Integer, Integer> stateIdToPaletteId;
    private long[] data;

    public ChunkSection() {
        this.blockCount = 0;
        this.palette = new ArrayList<>();
        this.paletteIdToStateId = new HashMap<>();
        this.stateIdToPaletteId = new HashMap<>();
        this.data = new long[256];
        addToPalette(0);
    }

    private void addToPalette(int stateId) {
        int index = palette.size();
        palette.add(stateId);
        paletteIdToStateId.put(index, stateId);
        stateIdToPaletteId.put(stateId, index);
    }

    private int getPaletteId(int x, int y, int z, int bitsPerBlock) {
        int index = (y << 8) | (z << 4) | x;
        int longIndex = (index * bitsPerBlock) >> 6;
        int bitOffset = (index * bitsPerBlock) & 63;
        long mask = (1L << bitsPerBlock) - 1;
        long shifted = data[longIndex] >>> bitOffset;
        if (bitOffset + bitsPerBlock <= 64) {
            return (int) (shifted & mask);
        } else {
            int lowBits = 64 - bitOffset;
            int highBits = bitsPerBlock - lowBits;
            long high = data[longIndex + 1];
            return (int) ((shifted | (high << lowBits)) & mask);
        }
    }

    public int getBlockState(int x, int y, int z) {
        int bitsPerBlock = bitsForPalette();
        int paletteId = getPaletteId(x, y, z, bitsPerBlock);
        return paletteIdToStateId.getOrDefault(paletteId, 0);
    }

    public void setBlockState(int x, int y, int z, int stateId) {
        int oldStateId = getBlockState(x, y, z);
        if (oldStateId == stateId) return;

        int newBitsPerBlock = bitsForPalette();
        int oldBitsPerBlock = newBitsPerBlock;

        int paletteId = stateIdToPaletteId.getOrDefault(stateId, -1);
        if (paletteId == -1) {
            paletteId = palette.size();
            addToPalette(stateId);
            newBitsPerBlock = bitsForPalette();
        }

        if (oldBitsPerBlock != newBitsPerBlock) {
            repackData(oldBitsPerBlock, newBitsPerBlock);
        }

        if (oldStateId == 0 && stateId != 0) {
            blockCount++;
        } else if (oldStateId != 0 && stateId == 0) {
            blockCount--;
        }

        int index = (y << 8) | (z << 4) | x;
        int longIndex = (index * newBitsPerBlock) >> 6;
        int bitOffset = (index * newBitsPerBlock) & 63;
        long mask = (1L << newBitsPerBlock) - 1;
        int lowBits = Math.min(newBitsPerBlock, 64 - bitOffset);

        data[longIndex] &= ~(mask << bitOffset);
        data[longIndex] |= ((long) paletteId & ((1L << lowBits) - 1)) << bitOffset;
        if (lowBits < newBitsPerBlock && longIndex < data.length - 1) {
            int highBits = newBitsPerBlock - lowBits;
            data[longIndex + 1] &= ~((1L << highBits) - 1);
            data[longIndex + 1] |= ((long) paletteId >> lowBits);
        }
    }

    private void repackData(int oldBits, int newBits) {
        int[] oldPaletteIds = new int[4096];
        for (int i = 0; i < 4096; i++) {
            int idx = i;
            int oldLongIndex = (idx * oldBits) >> 6;
            int oldBitOffset = (idx * oldBits) & 63;
            long oldMask = (1L << oldBits) - 1;
            long shifted = data[oldLongIndex] >>> oldBitOffset;
            if (oldBitOffset + oldBits <= 64) {
                oldPaletteIds[i] = (int) (shifted & oldMask);
            } else {
                int low = 64 - oldBitOffset;
                long high = data[oldLongIndex + 1];
                oldPaletteIds[i] = (int) ((shifted | (high << low)) & oldMask);
            }
        }

        int newLongs = (4096 * newBits + 63) / 64;
        data = new long[Math.max(newLongs, 256)];

        for (int i = 0; i < 4096; i++) {
            int pid = oldPaletteIds[i];
            int newLongIndex = (i * newBits) >> 6;
            int newBitOffset = (i * newBits) & 63;
            long newMask = (1L << newBits) - 1;
            int lowBits = Math.min(newBits, 64 - newBitOffset);

            data[newLongIndex] &= ~(newMask << newBitOffset);
            data[newLongIndex] |= ((long) pid & ((1L << lowBits) - 1)) << newBitOffset;
            if (lowBits < newBits && newLongIndex < data.length - 1) {
                int highBits = newBits - lowBits;
                data[newLongIndex + 1] &= ~((1L << highBits) - 1);
                data[newLongIndex + 1] |= ((long) pid >> lowBits);
            }
        }
    }

    private int bitsForPalette() {
        if (palette.size() <= 1) return 4;
        return Math.max(4, ceilLog2(palette.size()));
    }

    private static int ceilLog2(int value) {
        int bits = 0;
        int v = value - 1;
        while (v > 0) {
            v >>= 1;
            bits++;
        }
        return bits;
    }

    public byte[] toBytes() {
        int bitsPerBlock;
        boolean direct = palette.size() > 256;
        if (direct) {
            bitsPerBlock = 15;
        } else {
            bitsPerBlock = bitsForPalette();
        }

        int longsPerSection = (4096 * bitsPerBlock + 63) / 64;

        List<Byte> out = new ArrayList<>();

        writeShort(out, blockCount);

        out.add((byte) bitsPerBlock);

        if (!direct) {
            writeVarInt(out, palette.size());
            for (int stateId : palette) {
                writeVarInt(out, stateId);
            }
        }

        writeVarInt(out, longsPerSection);
        for (int i = 0; i < longsPerSection; i++) {
            long val = i < data.length ? data[i] : 0;
            writeLong(out, val);
        }

        byte[] result = new byte[out.size()];
        for (int i = 0; i < out.size(); i++) {
            result[i] = out.get(i);
        }
        return result;
    }

    public void setPaletteAndData(int[] palette, long[] data) {
        this.palette.clear();
        this.paletteIdToStateId.clear();
        this.stateIdToPaletteId.clear();
        for (int i = 0; i < palette.length; i++) {
            addToPalette(palette[i]);
        }
        this.data = data;
    }

    private static void writeShort(List<Byte> out, short value) {
        out.add((byte) ((value >> 8) & 0xFF));
        out.add((byte) (value & 0xFF));
    }

    private static void writeVarInt(List<Byte> out, int value) {
        while ((value & ~0x7F) != 0) {
            out.add((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.add((byte) value);
    }

    private static void writeLong(List<Byte> out, long value) {
        out.add((byte) ((value >> 56) & 0xFF));
        out.add((byte) ((value >> 48) & 0xFF));
        out.add((byte) ((value >> 40) & 0xFF));
        out.add((byte) ((value >> 32) & 0xFF));
        out.add((byte) ((value >> 24) & 0xFF));
        out.add((byte) ((value >> 16) & 0xFF));
        out.add((byte) ((value >> 8) & 0xFF));
        out.add((byte) (value & 0xFF));
    }
}
