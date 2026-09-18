package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class SetSimulationDistanceS2CCodec implements PacketCodec<SetSimulationDistanceS2C> {

    @Override
    public void encode(SetSimulationDistanceS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, packet.distance());
    }

    @Override
    public SetSimulationDistanceS2C decode(ByteBuf buf) {
        int distance = ByteBufVarInts.readVarInt(buf);
        return new SetSimulationDistanceS2C(distance);
    }
}
