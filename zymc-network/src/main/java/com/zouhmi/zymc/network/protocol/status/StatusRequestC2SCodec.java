package com.zouhmi.zymc.network.protocol.status;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class StatusRequestC2SCodec implements PacketCodec<StatusRequestC2S> {

    @Override
    public void encode(StatusRequestC2S packet, ByteBuf buf) {
        // No payload
    }

    @Override
    public StatusRequestC2S decode(ByteBuf buf) {
        return new StatusRequestC2S();
    }
}
