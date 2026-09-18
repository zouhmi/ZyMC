package com.zouhmi.zymc.world;

public class ChunkSection {
    private short blockCount;
    private short[] palette;
    private byte[] data;

    public ChunkSection() {
        this.blockCount = 0;
        this.palette = new short[]{0};
        this.data = new byte[2048];
    }

    public int getBlockState(int x, int y, int z) {
        return 0;
    }

    public void setBlockState(int x, int y, int z, int state) {
    }
}
