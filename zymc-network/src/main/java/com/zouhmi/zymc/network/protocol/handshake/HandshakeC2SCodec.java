package com.zouhmi.zymc.network.protocol.handshake;

import com.zouhmi.zymc.core.buffer.Strings;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public final class HandshakeC2SCodec implements PacketCodec<HandshakeC2S> {

    @Override
    public void encode(HandshakeC2S packet, ByteBuf buf) {
        try {
            writeVarInt(buf, packet.protocolVersion());
            Strings.writeString(buf::writeByte, packet.address());
            buf.writeShort(packet.port() & 0xFFFF);
            writeVarInt(buf, packet.intendedState().value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HandshakeC2S decode(ByteBuf buf) {
        try {
            int protocolVersion = readVarInt(buf);
            String address = Strings.readString(buf::readByte);
            int port = buf.readUnsignedShort();
            int intendedStateValue = readVarInt(buf);
            HandshakeC2S.ConnectionIntent intendedState = HandshakeC2S.ConnectionIntent.fromValue(intendedStateValue);
            return new HandshakeC2S(protocolVersion, address, port, intendedState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int readVarInt(ByteBuf in) {
        int out = 0;
        int bytes = 0;
        int b;
        while (bytes < 5) {
            if (!in.isReadable()) throw new RuntimeException("Unexpected end of stream reading VarInt");
            b = in.readByte() & 0xFF;
            out |= (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

    private static void writeVarInt(ByteBuf out, int value) {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.writeByte(part);
        } while (value != 0);
    }
}
