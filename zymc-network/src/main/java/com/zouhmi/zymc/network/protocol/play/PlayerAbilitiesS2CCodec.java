package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class PlayerAbilitiesS2CCodec implements PacketCodec<PlayerAbilitiesS2C> {

    @Override
    public void encode(PlayerAbilitiesS2C packet, ByteBuf buf) {
        buf.writeByte(packet.flags());
        buf.writeInt(Float.floatToRawIntBits(packet.flySpeed()));
        buf.writeInt(Float.floatToRawIntBits(packet.fov()));
    }

    @Override
    public PlayerAbilitiesS2C decode(ByteBuf buf) {
        byte flags = buf.readByte();
        float flySpeed = Float.intBitsToFloat(buf.readInt());
        float fov = Float.intBitsToFloat(buf.readInt());
        return new PlayerAbilitiesS2C(flags, flySpeed, fov);
    }
}
