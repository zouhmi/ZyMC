package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class PlayerPositionAndLookS2CCodec implements PacketCodec<PlayerPositionAndLookS2C> {

    @Override
    public void encode(PlayerPositionAndLookS2C packet, ByteBuf buf) {
        buf.writeDouble(packet.x());
        buf.writeDouble(packet.y());
        buf.writeDouble(packet.z());
        buf.writeInt(Float.floatToRawIntBits(packet.yaw()));
        buf.writeInt(Float.floatToRawIntBits(packet.pitch()));
        buf.writeByte(packet.flags());
        ByteBufVarInts.writeVarInt(buf, packet.teleportId());
    }

    @Override
    public PlayerPositionAndLookS2C decode(ByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = Float.intBitsToFloat(buf.readInt());
        float pitch = Float.intBitsToFloat(buf.readInt());
        byte flags = buf.readByte();
        int teleportId = ByteBufVarInts.readVarInt(buf);
        return new PlayerPositionAndLookS2C(x, y, z, yaw, pitch, flags, teleportId);
    }
}
