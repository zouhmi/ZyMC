package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class PingConfigurationS2CCodec implements PacketCodec<PingConfigurationS2C> {

    @Override
    public void encode(PingConfigurationS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, packet.id());
    }

    @Override
    public PingConfigurationS2C decode(ByteBuf buf) {
        int id = ByteBufVarInts.readVarInt(buf);
        return new PingConfigurationS2C(id);
    }
}
