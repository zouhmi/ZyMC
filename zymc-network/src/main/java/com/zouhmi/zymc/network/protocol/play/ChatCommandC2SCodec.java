package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class ChatCommandC2SCodec implements PacketCodec<ChatCommandC2S> {

    @Override
    public void encode(ChatCommandC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.command());
    }

    @Override
    public ChatCommandC2S decode(ByteBuf buf) {
        String command = ByteBufVarInts.readString(buf);
        return new ChatCommandC2S(command);
    }
}
