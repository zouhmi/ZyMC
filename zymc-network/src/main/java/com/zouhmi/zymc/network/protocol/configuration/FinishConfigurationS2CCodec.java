package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class FinishConfigurationS2CCodec implements PacketCodec<FinishConfigurationS2C> {

    @Override
    public void encode(FinishConfigurationS2C packet, ByteBuf buf) {
    }

    @Override
    public FinishConfigurationS2C decode(ByteBuf buf) {
        return new FinishConfigurationS2C();
    }
}
