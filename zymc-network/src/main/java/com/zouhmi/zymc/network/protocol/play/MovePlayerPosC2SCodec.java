package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class MovePlayerPosC2SCodec implements PacketCodec<MovePlayerPosC2S> {

    @Override
    public void encode(MovePlayerPosC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeBoolean(buf, packet.onGround());
        buf.writeDouble(packet.x());
        buf.writeDouble(packet.y());
        buf.writeDouble(packet.z());
    }

    @Override
    public MovePlayerPosC2S decode(ByteBuf buf) {
        boolean onGround = ByteBufVarInts.readBoolean(buf);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new MovePlayerPosC2S(onGround, x, y, z);
    }
}
