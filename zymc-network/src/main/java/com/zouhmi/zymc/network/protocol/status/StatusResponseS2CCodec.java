package com.zouhmi.zymc.network.protocol.status;

import com.zouhmi.zymc.core.buffer.Strings;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public final class StatusResponseS2CCodec implements PacketCodec<StatusResponseS2C> {

    @Override
    public void encode(StatusResponseS2C packet, ByteBuf buf) {
        try {
            Strings.writeString(buf::writeByte, packet.json());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StatusResponseS2C decode(ByteBuf buf) {
        try {
            String json = Strings.readString(buf::readByte);
            return new StatusResponseS2C(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
