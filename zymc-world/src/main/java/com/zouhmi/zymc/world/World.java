package com.zouhmi.zymc.world;

public class World {
    private final String name;
    private final long seed;
    private int seaLevel;

    public World(String name, long seed) {
        this.name = name;
        this.seed = seed;
        this.seaLevel = 64;
    }

    public String getName() {
        return name;
    }

    public long getSeed() {
        return seed;
    }

    public int getSeaLevel() {
        return seaLevel;
    }
}
