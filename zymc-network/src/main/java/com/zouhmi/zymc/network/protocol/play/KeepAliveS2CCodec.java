package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class KeepAliveS2CCodec implements PacketCodec<KeepAliveS2C> {

    @Override
    public void encode(KeepAliveS2C packet, ByteBuf buf) {
        buf.writeLong(packet.id());
    }

    @Override
    public KeepAliveS2C decode(ByteBuf buf) {
        long id = buf.readLong();
        return new KeepAliveS2C(id);
    }
}
