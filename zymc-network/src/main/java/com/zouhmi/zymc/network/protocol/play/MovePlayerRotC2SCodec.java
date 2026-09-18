package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class MovePlayerRotC2SCodec implements PacketCodec<MovePlayerRotC2S> {

    @Override
    public void encode(MovePlayerRotC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeBoolean(buf, packet.onGround());
        buf.writeInt(Float.floatToRawIntBits(packet.yaw()));
        buf.writeInt(Float.floatToRawIntBits(packet.pitch()));
    }

    @Override
    public MovePlayerRotC2S decode(ByteBuf buf) {
        boolean onGround = ByteBufVarInts.readBoolean(buf);
        float yaw = Float.intBitsToFloat(buf.readInt());
        float pitch = Float.intBitsToFloat(buf.readInt());
        return new MovePlayerRotC2S(onGround, yaw, pitch);
    }
}
