package com.zouhmi.zymc.entity;

import java.util.UUID;

public class PlayerEntity extends Entity {
    private final String name;
    private int gameMode;
    private float health = 20.0f;
    private int foodLevel = 20;
    private float saturation;

    public PlayerEntity(int entityId, UUID uuid, String name, double x, double y, double z) {
        super(entityId, uuid, x, y, z);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getGameMode() {
        return gameMode;
    }

    public float getHealth() {
        return health;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }
}
