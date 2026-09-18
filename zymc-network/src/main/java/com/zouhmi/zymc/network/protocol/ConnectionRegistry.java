package com.zouhmi.zymc.network.protocol;

import io.netty.util.AttributeKey;
import java.util.HashMap;
import java.util.Map;

public final class ConnectionRegistry {
    public static final AttributeKey<ConnectionState> STATE_KEY = AttributeKey.valueOf("zymc.connectionState");

    private final Map<ConnectionState, PacketRegistry> registries = new HashMap<>();

    public ConnectionRegistry() {
    }

    public PacketRegistry getRegistry(ConnectionState state) {
        return registries.computeIfAbsent(state, k -> new PacketRegistry());
    }

    public PacketRegistry registryFor(ConnectionState state) {
        return getRegistry(state);
    }

    public ConnectionState currentState() {
        return ConnectionState.HANDSHAKING;
    }

    public void setState(ConnectionState state) {
    }

    public <P extends Packet<?>> void register(ConnectionState state, int packetId, Class<P> packetType, PacketCodec<P> codec) {
        getRegistry(state).register(packetId, packetType, codec);
    }

    public <P extends Packet<?>> void addEncoder(ConnectionState state, Class<P> packetType, PacketCodec<P> codec, int packetId) {
        getRegistry(state).addEncoder(packetType, codec, packetId);
    }
}
