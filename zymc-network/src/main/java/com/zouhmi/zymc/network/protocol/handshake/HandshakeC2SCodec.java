package com.zouhmi.zymc.network.protocol.handshake;

import com.zouhmi.zymc.core.buffer.Strings;
import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public final class HandshakeC2SCodec implements PacketCodec<HandshakeC2S> {

    @Override
    public void encode(HandshakeC2S packet, ByteBuf buf) {
        try {
            ByteBufVarInts.writeVarInt(buf, packet.protocolVersion());
            Strings.writeString(buf::writeByte, packet.address());
            buf.writeShort(packet.port() & 0xFFFF);
            ByteBufVarInts.writeVarInt(buf, packet.intendedState().value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HandshakeC2S decode(ByteBuf buf) {
        try {
            int protocolVersion = ByteBufVarInts.readVarInt(buf);
            String address = Strings.readString(buf::readByte);
            int port = buf.readUnsignedShort();
            int intendedStateValue = ByteBufVarInts.readVarInt(buf);
            HandshakeC2S.ConnectionIntent intendedState = HandshakeC2S.ConnectionIntent.fromValue(intendedStateValue);
            return new HandshakeC2S(protocolVersion, address, port, intendedState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
