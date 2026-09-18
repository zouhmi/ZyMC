package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class SystemChatS2CCodec implements PacketCodec<SystemChatS2C> {

    @Override
    public void encode(SystemChatS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.message());
        ByteBufVarInts.writeBoolean(buf, packet.overlay());
    }

    @Override
    public SystemChatS2C decode(ByteBuf buf) {
        String message = ByteBufVarInts.readString(buf);
        boolean overlay = ByteBufVarInts.readBoolean(buf);
        return new SystemChatS2C(message, overlay);
    }
}
