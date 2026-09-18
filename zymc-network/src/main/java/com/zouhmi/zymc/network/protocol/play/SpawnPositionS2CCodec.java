package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class SpawnPositionS2CCodec implements PacketCodec<SpawnPositionS2C> {

    @Override
    public void encode(SpawnPositionS2C packet, ByteBuf buf) {
        buf.writeDouble(packet.x());
        buf.writeDouble(packet.y());
        buf.writeDouble(packet.z());
        buf.writeFloat(packet.angle());
    }

    @Override
    public SpawnPositionS2C decode(ByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float angle = buf.readFloat();
        return new SpawnPositionS2C(x, y, z, angle);
    }
}
