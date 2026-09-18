package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class DisconnectPlayS2CCodec implements PacketCodec<DisconnectPlayS2C> {

    @Override
    public void encode(DisconnectPlayS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.reason());
    }

    @Override
    public DisconnectPlayS2C decode(ByteBuf buf) {
        String reason = ByteBufVarInts.readString(buf);
        return new DisconnectPlayS2C(reason);
    }
}
