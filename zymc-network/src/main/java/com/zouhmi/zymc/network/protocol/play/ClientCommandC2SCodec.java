package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class ClientCommandC2SCodec implements PacketCodec<ClientCommandC2S> {

    @Override
    public void encode(ClientCommandC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, packet.actionId());
    }

    @Override
    public ClientCommandC2S decode(ByteBuf buf) {
        int actionId = ByteBufVarInts.readVarInt(buf);
        return new ClientCommandC2S(actionId);
    }
}
