package com.zouhmi.zymc.game;

public class ServerConfig {
    private String serverName = "ZyMC";
    private int port = 25565;
    private int maxPlayers = 20;
    private int viewDistance = 10;
    private int simulationDistance = 10;
    private String motd = "A Minecraft Server";
    private boolean onlineMode = false;
    private long seed = 0L;

    public ServerConfig() {
    }

    public String getServerName() {
        return serverName;
    }

    public int getPort() {
        return port;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public int getSimulationDistance() {
        return simulationDistance;
    }

    public String getMotd() {
        return motd;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public long getSeed() {
        return seed;
    }
}
