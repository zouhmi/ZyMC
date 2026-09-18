package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class GameEventS2CCodec implements PacketCodec<GameEventS2C> {

    @Override
    public void encode(GameEventS2C packet, ByteBuf buf) {
        buf.writeByte(packet.eventId());
        buf.writeInt(Float.floatToRawIntBits(packet.value()));
    }

    @Override
    public GameEventS2C decode(ByteBuf buf) {
        byte eventId = buf.readByte();
        float value = Float.intBitsToFloat(buf.readInt());
        return new GameEventS2C(eventId, value);
    }
}
