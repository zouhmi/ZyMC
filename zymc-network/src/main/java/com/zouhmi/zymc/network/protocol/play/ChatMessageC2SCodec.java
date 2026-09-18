package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class ChatMessageC2SCodec implements PacketCodec<ChatMessageC2S> {

    @Override
    public void encode(ChatMessageC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.message());
    }

    @Override
    public ChatMessageC2S decode(ByteBuf buf) {
        String message = ByteBufVarInts.readString(buf);
        return new ChatMessageC2S(message);
    }
}
