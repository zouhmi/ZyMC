package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class SetCenterChunkS2CCodec implements PacketCodec<SetCenterChunkS2C> {

    @Override
    public void encode(SetCenterChunkS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, packet.chunkX());
        ByteBufVarInts.writeVarInt(buf, packet.chunkZ());
    }

    @Override
    public SetCenterChunkS2C decode(ByteBuf buf) {
        int chunkX = ByteBufVarInts.readVarInt(buf);
        int chunkZ = ByteBufVarInts.readVarInt(buf);
        return new SetCenterChunkS2C(chunkX, chunkZ);
    }
}
