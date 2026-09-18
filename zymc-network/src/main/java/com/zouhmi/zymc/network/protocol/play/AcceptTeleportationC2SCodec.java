package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class AcceptTeleportationC2SCodec implements PacketCodec<AcceptTeleportationC2S> {

    @Override
    public void encode(AcceptTeleportationC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeVarInt(buf, packet.teleportId());
    }

    @Override
    public AcceptTeleportationC2S decode(ByteBuf buf) {
        int teleportId = ByteBufVarInts.readVarInt(buf);
        return new AcceptTeleportationC2S(teleportId);
    }
}
