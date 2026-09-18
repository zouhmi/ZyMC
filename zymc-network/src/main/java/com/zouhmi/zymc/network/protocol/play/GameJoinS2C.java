package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;
import java.util.Set;

public final class GameJoinS2C implements Packet<GameJoinS2C.ClientPlayListener> {
    private final int playerEntityId;
    private final boolean hardcore;
    private final Set<DimensionKey> dimensionIds;
    private final int maxPlayers;
    private final int viewDistance;
    private final int simulationDistance;
    private final boolean reducedDebugInfo;
    private final boolean showDeathScreen;
    private final boolean doLimitedCrafting;
    private final CommonSpawnInfo commonPlayerSpawnInfo;
    private final boolean enforcesSecureChat;

    public GameJoinS2C(int playerEntityId, boolean hardcore, Set<DimensionKey> dimensionIds,
                        int maxPlayers, int viewDistance, int simulationDistance,
                        boolean reducedDebugInfo, boolean showDeathScreen, boolean doLimitedCrafting,
                        CommonSpawnInfo commonPlayerSpawnInfo, boolean enforcesSecureChat) {
        this.playerEntityId = playerEntityId;
        this.hardcore = hardcore;
        this.dimensionIds = dimensionIds;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
        this.reducedDebugInfo = reducedDebugInfo;
        this.showDeathScreen = showDeathScreen;
        this.doLimitedCrafting = doLimitedCrafting;
        this.commonPlayerSpawnInfo = commonPlayerSpawnInfo;
        this.enforcesSecureChat = enforcesSecureChat;
    }

    public int playerEntityId() {
        return playerEntityId;
    }

    public boolean hardcore() {
        return hardcore;
    }

    public Set<DimensionKey> dimensionIds() {
        return dimensionIds;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public int viewDistance() {
        return viewDistance;
    }

    public int simulationDistance() {
        return simulationDistance;
    }

    public boolean reducedDebugInfo() {
        return reducedDebugInfo;
    }

    public boolean showDeathScreen() {
        return showDeathScreen;
    }

    public boolean doLimitedCrafting() {
        return doLimitedCrafting;
    }

    public CommonSpawnInfo commonPlayerSpawnInfo() {
        return commonPlayerSpawnInfo;
    }

    public boolean enforcesSecureChat() {
        return enforcesSecureChat;
    }

    @Override
    public void handle(ClientPlayListener listener) {
        listener.handle(this);
    }

    public interface ClientPlayListener {
        void handle(GameJoinS2C join);
    }

    public static final class DimensionKey {
        private final String registryNamespace;
        private final String registryName;
        private final String valueNamespace;
        private final String valueName;

        public DimensionKey(String registryNamespace, String registryName,
                            String valueNamespace, String valueName) {
            this.registryNamespace = registryNamespace;
            this.registryName = registryName;
            this.valueNamespace = valueNamespace;
            this.valueName = valueName;
        }

        public String registryNamespace() {
            return registryNamespace;
        }

        public String registryName() {
            return registryName;
        }

        public String valueNamespace() {
            return valueNamespace;
        }

        public String valueName() {
            return valueName;
        }
    }

    public static final class CommonSpawnInfo {
        private final DimensionKey dimensionType;
        private final DimensionKey dimension;
        private final long seed;
        private final int gameMode; // 0=SURVIVAL, 1=CREATIVE, 2=ADVENTURE, 3=SPECTATOR
        private final int lastGameMode;
        private final boolean isDebug;
        private final boolean isFlat;
        private final boolean hasDeathLocation;
        private final int portalCooldown;
        private final int seaLevel;

        public CommonSpawnInfo(DimensionKey dimensionType, DimensionKey dimension, long seed,
                               int gameMode, int lastGameMode, boolean isDebug, boolean isFlat,
                               boolean hasDeathLocation, int portalCooldown, int seaLevel) {
            this.dimensionType = dimensionType;
            this.dimension = dimension;
            this.seed = seed;
            this.gameMode = gameMode;
            this.lastGameMode = lastGameMode;
            this.isDebug = isDebug;
            this.isFlat = isFlat;
            this.hasDeathLocation = hasDeathLocation;
            this.portalCooldown = portalCooldown;
            this.seaLevel = seaLevel;
        }

        public DimensionKey dimensionType() {
            return dimensionType;
        }

        public DimensionKey dimension() {
            return dimension;
        }

        public long seed() {
            return seed;
        }

        public int gameMode() {
            return gameMode;
        }

        public int lastGameMode() {
            return lastGameMode;
        }

        public boolean isDebug() {
            return isDebug;
        }

        public boolean isFlat() {
            return isFlat;
        }

        public boolean hasDeathLocation() {
            return hasDeathLocation;
        }

        public int portalCooldown() {
            return portalCooldown;
        }

        public int seaLevel() {
            return seaLevel;
        }
    }
}
