package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class MovePlayerPosRotC2SCodec implements PacketCodec<MovePlayerPosRotC2S> {

    @Override
    public void encode(MovePlayerPosRotC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeBoolean(buf, packet.onGround());
        buf.writeDouble(packet.x());
        buf.writeDouble(packet.y());
        buf.writeDouble(packet.z());
        buf.writeInt(Float.floatToRawIntBits(packet.yaw()));
        buf.writeInt(Float.floatToRawIntBits(packet.pitch()));
    }

    @Override
    public MovePlayerPosRotC2S decode(ByteBuf buf) {
        boolean onGround = ByteBufVarInts.readBoolean(buf);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = Float.intBitsToFloat(buf.readInt());
        float pitch = Float.intBitsToFloat(buf.readInt());
        return new MovePlayerPosRotC2S(onGround, x, y, z, yaw, pitch);
    }
}
