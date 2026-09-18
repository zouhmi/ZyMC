package com.zouhmi.zymc.network.protocol;

import io.netty.buffer.ByteBuf;

public interface PacketCodec<P extends Packet<?>> {
    void encode(P packet, ByteBuf buf);

    P decode(ByteBuf buf);
}
