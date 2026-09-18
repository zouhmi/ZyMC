package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class MovePlayerStatusOnlyC2SCodec implements PacketCodec<MovePlayerStatusOnlyC2S> {

    @Override
    public void encode(MovePlayerStatusOnlyC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeBoolean(buf, packet.onGround());
    }

    @Override
    public MovePlayerStatusOnlyC2S decode(ByteBuf buf) {
        boolean onGround = ByteBufVarInts.readBoolean(buf);
        return new MovePlayerStatusOnlyC2S(onGround);
    }
}
