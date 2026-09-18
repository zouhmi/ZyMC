package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Set;

public final class GameJoinS2CCodec implements PacketCodec<GameJoinS2C> {

    @Override
    public void encode(GameJoinS2C packet, ByteBuf buf) {
        try {
            writeVarInt(buf, packet.playerEntityId());
            writeBoolean(buf, packet.hardcore());
            writeDimensionKeySet(buf, packet.dimensionIds());
            writeVarInt(buf, packet.maxPlayers());
            writeVarInt(buf, packet.viewDistance());
            writeVarInt(buf, packet.simulationDistance());
            writeBoolean(buf, packet.reducedDebugInfo());
            writeBoolean(buf, packet.showDeathScreen());
            writeBoolean(buf, packet.doLimitedCrafting());
            writeCommonSpawnInfo(buf, packet.commonPlayerSpawnInfo());
            writeBoolean(buf, packet.enforcesSecureChat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameJoinS2C decode(ByteBuf buf) {
        try {
            int playerEntityId = readVarInt(buf);
            boolean hardcore = readBoolean(buf);
            Set<GameJoinS2C.DimensionKey> dimensionIds = readDimensionKeySet(buf);
            int maxPlayers = readVarInt(buf);
            int viewDistance = readVarInt(buf);
            int simulationDistance = readVarInt(buf);
            boolean reducedDebugInfo = readBoolean(buf);
            boolean showDeathScreen = readBoolean(buf);
            boolean doLimitedCrafting = readBoolean(buf);
            GameJoinS2C.CommonSpawnInfo spawnInfo = readCommonSpawnInfo(buf);
            boolean enforcesSecureChat = readBoolean(buf);
            return new GameJoinS2C(playerEntityId, hardcore, dimensionIds,
                    maxPlayers, viewDistance, simulationDistance,
                    reducedDebugInfo, showDeathScreen, doLimitedCrafting,
                    spawnInfo, enforcesSecureChat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- helpers ---

    private static void writeVarInt(ByteBuf out, int value) throws IOException {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }

    private static int readVarInt(ByteBuf in) throws IOException {
        int out = 0;
        int bytes = 0;
        int b;
        while (bytes < 5) {
            if (!in.isReadable()) throw new IOException("Unexpected end of stream reading VarInt");
            b = in.readByte() & 0xFF;
            out |= (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

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

    private static void writeBoolean(ByteBuf out, boolean value) {
        out.writeByte(value ? 0x01 : 0x00);
    }

    private static boolean readBoolean(ByteBuf in) {
        return in.readByte() != 0x00;
    }

    private static void writeIdentifier(ByteBuf out, String namespace, String name) throws IOException {
        String s = namespace + ":" + name;
        writeVarInt(out, s.length());
        out.writeBytes(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String[] readIdentifier(ByteBuf in) throws IOException {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
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
        writeVarInt(out, keys.size());
        for (GameJoinS2C.DimensionKey key : keys) {
            writeDimensionKey(out, key);
        }
    }

    private static Set<GameJoinS2C.DimensionKey> readDimensionKeySet(ByteBuf in) throws IOException {
        int count = readVarInt(in);
        for (int i = 0; i < count; i++) {
            readDimensionKey(in);
        }
        return java.util.Collections.emptySet();
    }

    private static void writeCommonSpawnInfo(ByteBuf out, GameJoinS2C.CommonSpawnInfo info) throws IOException {
        writeDimensionKey(out, info.dimensionType());
        writeDimensionKey(out, info.dimension());
        writeLong(out, info.seed());
        writeVarInt(out, info.gameMode());
        writeVarInt(out, info.lastGameMode());
        writeBoolean(out, info.isDebug());
        writeBoolean(out, info.isFlat());
        writeBoolean(out, info.hasDeathLocation());
        if (info.hasDeathLocation()) {
            writeDimensionKey(out, info.dimensionType());
            writeLong(out, 0); // x
            writeLong(out, 0); // y
            writeLong(out, 0); // z
        }
        writeVarInt(out, info.portalCooldown());
        writeVarInt(out, info.seaLevel());
    }

    private static GameJoinS2C.CommonSpawnInfo readCommonSpawnInfo(ByteBuf in) throws IOException {
        GameJoinS2C.DimensionKey dimensionType = readDimensionKey(in);
        GameJoinS2C.DimensionKey dimension = readDimensionKey(in);
        long seed = readLong(in);
        int gameMode = readVarInt(in);
        int lastGameMode = readVarInt(in);
        boolean isDebug = readBoolean(in);
        boolean isFlat = readBoolean(in);
        boolean hasDeathLocation = readBoolean(in);
        if (hasDeathLocation) {
            readDimensionKey(in);
            readLong(in);
            readLong(in);
            readLong(in);
        }
        int portalCooldown = readVarInt(in);
        int seaLevel = readVarInt(in);
        return new GameJoinS2C.CommonSpawnInfo(dimensionType, dimension, seed,
                gameMode, lastGameMode, isDebug, isFlat, hasDeathLocation,
                portalCooldown, seaLevel);
    }
}
