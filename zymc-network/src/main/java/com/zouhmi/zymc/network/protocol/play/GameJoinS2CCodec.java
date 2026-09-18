package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Set;

public final class GameJoinS2CCodec implements PacketCodec<GameJoinS2C> {

    @Override
    public void encode(GameJoinS2C packet, ByteBuf buf) {
        try {
            ByteBufVarInts.writeVarInt(buf, packet.playerEntityId());
            ByteBufVarInts.writeBoolean(buf, packet.hardcore());
            writeDimensionKeySet(buf, packet.dimensionIds());
            ByteBufVarInts.writeVarInt(buf, packet.maxPlayers());
            ByteBufVarInts.writeVarInt(buf, packet.viewDistance());
            ByteBufVarInts.writeVarInt(buf, packet.simulationDistance());
            ByteBufVarInts.writeBoolean(buf, packet.reducedDebugInfo());
            ByteBufVarInts.writeBoolean(buf, packet.showDeathScreen());
            ByteBufVarInts.writeBoolean(buf, packet.doLimitedCrafting());
            writeCommonSpawnInfo(buf, packet.commonPlayerSpawnInfo());
            ByteBufVarInts.writeBoolean(buf, packet.enforcesSecureChat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameJoinS2C decode(ByteBuf buf) {
        try {
            int playerEntityId = ByteBufVarInts.readVarInt(buf);
            boolean hardcore = ByteBufVarInts.readBoolean(buf);
            Set<GameJoinS2C.DimensionKey> dimensionIds = readDimensionKeySet(buf);
            int maxPlayers = ByteBufVarInts.readVarInt(buf);
            int viewDistance = ByteBufVarInts.readVarInt(buf);
            int simulationDistance = ByteBufVarInts.readVarInt(buf);
            boolean reducedDebugInfo = ByteBufVarInts.readBoolean(buf);
            boolean showDeathScreen = ByteBufVarInts.readBoolean(buf);
            boolean doLimitedCrafting = ByteBufVarInts.readBoolean(buf);
            GameJoinS2C.CommonSpawnInfo spawnInfo = readCommonSpawnInfo(buf);
            boolean enforcesSecureChat = ByteBufVarInts.readBoolean(buf);
            return new GameJoinS2C(playerEntityId, hardcore, dimensionIds,
                    maxPlayers, viewDistance, simulationDistance,
                    reducedDebugInfo, showDeathScreen, doLimitedCrafting,
                    spawnInfo, enforcesSecureChat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- helpers ---

    private static void writeLong(ByteBuf out, long value) {
        for (int i = 7; i >= 0; i--) {
            out.writeByte((int) ((value >> (8 * i)) & 0xFF));
        }
    }

    private static long readLong(ByteBuf in) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (in.readByte() & 0xFF);
        }
        return value;
    }

    private static void writeIdentifier(ByteBuf out, String namespace, String name) throws IOException {
        ByteBufVarInts.writeString(out, namespace + ":" + name);
    }

    private static String[] readIdentifier(ByteBuf in) throws IOException {
        String s = ByteBufVarInts.readString(in);
        int colon = s.indexOf(':');
        if (colon < 0) {
            throw new IOException("Invalid identifier: " + s);
        }
        return new String[]{s.substring(0, colon), s.substring(colon + 1)};
    }

    private static void writeDimensionKey(ByteBuf out, GameJoinS2C.DimensionKey key) throws IOException {
        writeIdentifier(out, key.registryNamespace(), key.registryName());
        writeIdentifier(out, key.valueNamespace(), key.valueName());
    }

    private static GameJoinS2C.DimensionKey readDimensionKey(ByteBuf in) throws IOException {
        String[] registry = readIdentifier(in);
        String[] value = readIdentifier(in);
        return new GameJoinS2C.DimensionKey(registry[0], registry[1], value[0], value[1]);
    }

    private static void writeDimensionKeySet(ByteBuf out, Set<GameJoinS2C.DimensionKey> keys) throws IOException {
        ByteBufVarInts.writeVarInt(out, keys.size());
        for (GameJoinS2C.DimensionKey key : keys) {
            writeDimensionKey(out, key);
        }
    }

    private static Set<GameJoinS2C.DimensionKey> readDimensionKeySet(ByteBuf in) throws IOException {
        int count = ByteBufVarInts.readVarInt(in);
        java.util.Set<GameJoinS2C.DimensionKey> keys = new java.util.HashSet<>();
        for (int i = 0; i < count; i++) {
            keys.add(readDimensionKey(in));
        }
        return keys;
    }

    private static void writeCommonSpawnInfo(ByteBuf out, GameJoinS2C.CommonSpawnInfo info) throws IOException {
        writeDimensionKey(out, info.dimensionType());
        writeDimensionKey(out, info.dimension());
        writeLong(out, info.seed());
        ByteBufVarInts.writeVarInt(out, info.gameMode());
        ByteBufVarInts.writeVarInt(out, info.lastGameMode());
        ByteBufVarInts.writeBoolean(out, info.isDebug());
        ByteBufVarInts.writeBoolean(out, info.isFlat());
        ByteBufVarInts.writeBoolean(out, info.hasDeathLocation());
        if (info.hasDeathLocation()) {
            writeDimensionKey(out, info.dimensionType());
            writeLong(out, 0); // x
            writeLong(out, 0); // y
            writeLong(out, 0); // z
        }
        ByteBufVarInts.writeVarInt(out, info.portalCooldown());
        ByteBufVarInts.writeVarInt(out, info.seaLevel());
    }

    private static GameJoinS2C.CommonSpawnInfo readCommonSpawnInfo(ByteBuf in) throws IOException {
        GameJoinS2C.DimensionKey dimensionType = readDimensionKey(in);
        GameJoinS2C.DimensionKey dimension = readDimensionKey(in);
        long seed = readLong(in);
        int gameMode = ByteBufVarInts.readVarInt(in);
        int lastGameMode = ByteBufVarInts.readVarInt(in);
        boolean isDebug = ByteBufVarInts.readBoolean(in);
        boolean isFlat = ByteBufVarInts.readBoolean(in);
        boolean hasDeathLocation = ByteBufVarInts.readBoolean(in);
        if (hasDeathLocation) {
            readDimensionKey(in);
            readLong(in);
            readLong(in);
            readLong(in);
        }
        int portalCooldown = ByteBufVarInts.readVarInt(in);
        int seaLevel = ByteBufVarInts.readVarInt(in);
        return new GameJoinS2C.CommonSpawnInfo(dimensionType, dimension, seed,
                gameMode, lastGameMode, isDebug, isFlat, hasDeathLocation,
                portalCooldown, seaLevel);
    }
}
