package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class KeepAliveC2SCodec implements PacketCodec<KeepAliveC2S> {

    @Override
    public void encode(KeepAliveC2S packet, ByteBuf buf) {
        buf.writeLong(packet.id());
    }

    @Override
    public KeepAliveC2S decode(ByteBuf buf) {
        long id = buf.readLong();
        return new KeepAliveC2S(id);
    }
}
