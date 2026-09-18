package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class ChunkDataAndUpdateLightS2CCodec implements PacketCodec<ChunkDataAndUpdateLightS2C> {

    @Override
    public void encode(ChunkDataAndUpdateLightS2C packet, ByteBuf buf) {
        buf.writeInt(packet.chunkX());
        buf.writeInt(packet.chunkZ());
        ByteBufVarInts.writeBytes(buf, packet.chunkData());
    }

    @Override
    public ChunkDataAndUpdateLightS2C decode(ByteBuf buf) {
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        byte[] chunkData = ByteBufVarInts.readBytes(buf);
        return new ChunkDataAndUpdateLightS2C(chunkX, chunkZ, chunkData);
    }
}
