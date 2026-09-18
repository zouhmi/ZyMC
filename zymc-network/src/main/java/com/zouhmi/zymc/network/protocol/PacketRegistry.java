package com.zouhmi.zymc.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PacketRegistry {
    private final Map<Integer, PacketCodec<?>> decodeCodecs = new HashMap<>();
    private final Map<Class<?>, PacketCodec<?>> encodeCodecs = new HashMap<>();
    private final Map<Class<?>, Integer> packetIds = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <P extends Packet<?>> void register(int packetId, Class<P> packetType, PacketCodec<P> codec) {
        decodeCodecs.put(packetId, codec);
        encodeCodecs.put(packetType, codec);
        packetIds.put(packetType, packetId);
    }

    public <P extends Packet<?>> void addEncoder(Class<P> packetType, PacketCodec<P> codec, int packetId) {
        encodeCodecs.put(packetType, codec);
        packetIds.put(packetType, packetId);
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet<?>> P decode(int packetId, ByteBuf buf) {
        PacketCodec<P> codec = (PacketCodec<P>) decodeCodecs.get(packetId);
        if (codec == null) {
            throw new IllegalArgumentException("Unknown packet ID in current state: " + packetId);
        }
        return codec.decode(buf);
    }

    public void encode(Packet<?> packet, ByteBuf buf) {
        @SuppressWarnings("unchecked")
        PacketCodec<Packet<?>> codec = (PacketCodec<Packet<?>>) encodeCodecs.get(packet.getClass());
        if (codec == null) {
            throw new IllegalArgumentException("No codec registered for packet type: " + packet.getClass().getName());
        }
        codec.encode(packet, buf);
    }

    public int packetId(Packet<?> packet) {
        Integer id = packetIds.get(packet.getClass());
        if (id == null) {
            throw new IllegalArgumentException("No packet ID registered for packet type: " + packet.getClass().getName());
        }
        return id;
    }
}
