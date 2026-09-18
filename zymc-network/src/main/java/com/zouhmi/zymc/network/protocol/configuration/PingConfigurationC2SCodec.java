package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class PingConfigurationC2SCodec implements PacketCodec<PingConfigurationC2S> {

    @Override
    public void encode(PingConfigurationC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, packet.id());
    }

    @Override
    public PingConfigurationC2S decode(ByteBuf buf) {
        int id = ByteBufVarInts.readVarInt(buf);
        return new PingConfigurationC2S(id);
    }
}
