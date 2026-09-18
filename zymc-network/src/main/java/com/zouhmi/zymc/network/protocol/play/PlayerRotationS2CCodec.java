package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class PlayerRotationS2CCodec implements PacketCodec<PlayerRotationS2C> {

    @Override
    public void encode(PlayerRotationS2C packet, ByteBuf buf) {
        buf.writeInt(Float.floatToRawIntBits(packet.yaw()));
        buf.writeInt(Float.floatToRawIntBits(packet.pitch()));
        ByteBufVarInts.writeBoolean(buf, packet.onGround());
    }

    @Override
    public PlayerRotationS2C decode(ByteBuf buf) {
        float yaw = Float.intBitsToFloat(buf.readInt());
        float pitch = Float.intBitsToFloat(buf.readInt());
        boolean onGround = ByteBufVarInts.readBoolean(buf);
        return new PlayerRotationS2C(yaw, pitch, onGround);
    }
}
