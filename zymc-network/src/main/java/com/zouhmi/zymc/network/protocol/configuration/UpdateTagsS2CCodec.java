package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class UpdateTagsS2CCodec implements PacketCodec<UpdateTagsS2C> {

    @Override
    public void encode(UpdateTagsS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, 0);
    }

    @Override
    public UpdateTagsS2C decode(ByteBuf buf) {
        ByteBufVarInts.readVarInt(buf);
        return new UpdateTagsS2C();
    }
}
