package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class KnownPacksS2CCodec implements PacketCodec<KnownPacksS2C> {

    @Override
    public void encode(KnownPacksS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, 1);
        ByteBufVarInts.writeString(buf, packet.namespace());
        ByteBufVarInts.writeString(buf, packet.id());
        ByteBufVarInts.writeString(buf, packet.version());
    }

    @Override
    public KnownPacksS2C decode(ByteBuf buf) {
        ByteBufVarInts.readVarInt(buf);
        String namespace = ByteBufVarInts.readString(buf);
        String id = ByteBufVarInts.readString(buf);
        String version = ByteBufVarInts.readString(buf);
        return new KnownPacksS2C(namespace, id, version);
    }
}
