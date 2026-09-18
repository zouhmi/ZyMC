package com.zouhmi.zymc.world;

import java.util.HashMap;
import java.util.Map;

public class FlatWorldGenerator {
    private final Map<String, ChunkColumn> chunks;

    public FlatWorldGenerator() {
        this.chunks = new HashMap<>();
    }

    public ChunkColumn getChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        ChunkColumn cached = chunks.get(key);
        if (cached != null) {
            return cached;
        }

        ChunkColumn chunk = new ChunkColumn(chunkX, chunkZ);

        ChunkSection section4 = chunk.getSection(4);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                section4.setBlockState(x, 0, z, 79);
                section4.setBlockState(x, 1, z, 2);
                section4.setBlockState(x, 2, z, 2);
                section4.setBlockState(x, 3, z, 2);
                section4.setBlockState(x, 4, z, 3);
            }
        }

        chunks.put(key, chunk);
        return chunk;
    }

    public void clearCache() {
        chunks.clear();
    }
}
