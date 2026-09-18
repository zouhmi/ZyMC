package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class FinishConfigurationC2SCodec implements PacketCodec<FinishConfigurationC2S> {

    @Override
    public void encode(FinishConfigurationC2S packet, ByteBuf buf) {
    }

    @Override
    public FinishConfigurationC2S decode(ByteBuf buf) {
        return new FinishConfigurationC2S();
    }
}
