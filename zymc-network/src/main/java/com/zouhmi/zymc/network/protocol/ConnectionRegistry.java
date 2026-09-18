package com.zouhmi.zymc.network.protocol;

import java.util.HashMap;
import java.util.Map;

public final class ConnectionRegistry {
    private final Map<ConnectionState, PacketRegistry> registries = new HashMap<>();
    private volatile ConnectionState currentState = ConnectionState.HANDSHAKING;

    public ConnectionRegistry() {
    }

    public PacketRegistry getRegistry(ConnectionState state) {
        return registries.computeIfAbsent(state, k -> new PacketRegistry());
    }

    public PacketRegistry currentRegistry() {
        return getRegistry(currentState);
    }

    public ConnectionState currentState() {
        return currentState;
    }

    public void setState(ConnectionState state) {
        this.currentState = state;
    }

    public <P extends Packet<?>> void register(ConnectionState state, int packetId, Class<P> packetType, PacketCodec<P> codec) {
        getRegistry(state).register(packetId, packetType, codec);
    }

    public <P extends Packet<?>> void addEncoder(ConnectionState state, Class<P> packetType, PacketCodec<P> codec, int packetId) {
        getRegistry(state).addEncoder(packetType, codec, packetId);
    }
}
