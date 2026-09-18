package com.zouhmi.zymc.world.registries;

import com.zouhmi.zymc.world.BlockState;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<Integer, BlockState> blocks = new HashMap<>();

    public static void init() {
        blocks.put(0, BlockState.AIR);
        blocks.put(1, new BlockState(1, "minecraft:stone", true, false, 1.5f, 6.0f));
        blocks.put(2, new BlockState(2, "minecraft:dirt", true, false, 0.5f, 0.5f));
        blocks.put(3, new BlockState(3, "minecraft:grass_block", true, false, 0.6f, 0.6f));
        blocks.put(79, new BlockState(79, "minecraft:bedrock", true, false, -1.0f, 18000000.0f));
        blocks.put(9, new BlockState(9, "minecraft:water", false, true, 100.0f, 500.0f));
        blocks.put(11, new BlockState(11, "minecraft:lava", false, true, 100.0f, 500.0f));
        blocks.put(15, new BlockState(15, "minecraft:oak_planks", true, false, 2.0f, 3.0f));
    }

    public static BlockState get(int id) {
        return blocks.get(id);
    }
}
