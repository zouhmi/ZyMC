package com.zouhmi.zymc.world;

public class BlockState {
    public static final BlockState AIR = new BlockState(0, "minecraft:air", false, false, 0, 0);

    private final int id;
    private final String name;
    private final boolean solid;
    private final boolean liquid;
    private final float destroyTime;
    private final float explosionResistance;

    public BlockState(int id, String name, boolean solid, boolean liquid, float destroyTime, float explosionResistance) {
        this.id = id;
        this.name = name;
        this.solid = solid;
        this.liquid = liquid;
        this.destroyTime = destroyTime;
        this.explosionResistance = explosionResistance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public float getDestroyTime() {
        return destroyTime;
    }

    public float getExplosionResistance() {
        return explosionResistance;
    }
}
