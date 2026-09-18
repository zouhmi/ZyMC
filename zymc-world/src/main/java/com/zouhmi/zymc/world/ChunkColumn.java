package com.zouhmi.zymc.world;

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
        this.biomes = new int[0];
    }

    public ChunkSection getSection(int index) {
        return sections[index];
    }

    public byte[] toBytes() {
        return new byte[0];
    }
}
