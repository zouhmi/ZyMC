package com.zouhmi.zymc.world;

import java.util.ArrayList;
import java.util.List;

public class ChunkColumn {
    private final int x;
    private final int z;
    private final ChunkSection[] sections;
    private byte[] heightmaps;
    private int[] biomes;

    public ChunkColumn(int x, int z) {
        this.x = x;
        this.z = z;
        this.sections = new ChunkSection[24];
        for (int i = 0; i < 24; i++) {
            this.sections[i] = new ChunkSection();
        }
        this.heightmaps = new byte[0];
        this.biomes = new int[1024];
        for (int i = 0; i < 1024; i++) {
            this.biomes[i] = 39;
        }
    }

    public ChunkSection getSection(int index) {
        return sections[index];
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public byte[] toNetworkBytes() {
        List<Byte> out = new ArrayList<>();

        writeVarInt(out, 0);

        for (ChunkSection section : sections) {
            byte[] sectionBytes = section.toBytes();
            for (byte b : sectionBytes) {
                out.add(b);
            }
        }

        writeVarInt(out, 1024);
        for (int biome : biomes) {
            writeVarInt(out, biome);
        }

        byte[] result = new byte[out.size()];
        for (int i = 0; i < out.size(); i++) {
            result[i] = out.get(i);
        }
        return result;
    }

    private static void writeVarInt(List<Byte> out, int value) {
        while ((value & ~0x7F) != 0) {
            out.add((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.add((byte) value);
    }
}
