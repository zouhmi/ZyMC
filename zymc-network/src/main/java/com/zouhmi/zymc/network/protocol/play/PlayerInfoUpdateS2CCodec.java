package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public final class PlayerInfoUpdateS2CCodec implements PacketCodec<PlayerInfoUpdateS2C> {

    @Override
    public void encode(PlayerInfoUpdateS2C packet, ByteBuf buf) {
        buf.writeByte(packet.action());
        ByteBufVarInts.writeVarInt(buf, packet.uuids().length);
        for (UUID uuid : packet.uuids()) {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
        }
    }

    @Override
    public PlayerInfoUpdateS2C decode(ByteBuf buf) {
        byte action = buf.readByte();
        int count = ByteBufVarInts.readVarInt(buf);
        UUID[] uuids = new UUID[count];
        for (int i = 0; i < count; i++) {
            long mostSigBits = buf.readLong();
            long leastSigBits = buf.readLong();
            uuids[i] = new UUID(mostSigBits, leastSigBits);
        }
        return new PlayerInfoUpdateS2C(action, uuids);
    }
}
