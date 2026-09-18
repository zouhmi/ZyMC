package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class SetTimeS2CCodec implements PacketCodec<SetTimeS2C> {

    @Override
    public void encode(SetTimeS2C packet, ByteBuf buf) {
        buf.writeLong(packet.worldAge());
        buf.writeLong(packet.timeOfDay());
        ByteBufVarInts.writeBoolean(buf, packet.timeLocked());
    }

    @Override
    public SetTimeS2C decode(ByteBuf buf) {
        long worldAge = buf.readLong();
        long timeOfDay = buf.readLong();
        boolean timeLocked = ByteBufVarInts.readBoolean(buf);
        return new SetTimeS2C(worldAge, timeOfDay, timeLocked);
    }
}
