package com.zouhmi.zymc.network.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public final class NettyPacketDecoder extends ByteToMessageDecoder {

    private enum ParseState {
        LENGTH,
        ID,
        BODY
    }

    private ParseState state = ParseState.LENGTH;
    private int packetLength = 0;
    private int packetId = 0;
    private int bodyLength = 0;
    private final PacketRegistry registry;

    public NettyPacketDecoder(PacketRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state) {
            case LENGTH -> {
                if (!in.isReadable()) return;
                packetLength = readVarInt(in);
                if (packetLength < 0) {
                    throw new IllegalArgumentException("Bad packet length: " + packetLength);
                }
                state = ParseState.ID;
            }
            case ID -> {
                if (!in.isReadable()) return;
                packetId = readVarInt(in);
                // packetLength includes the ID and body; subtract the ID bytes already consumed.
                int idBytesConsumed = varIntSize(packetId);
                bodyLength = packetLength - idBytesConsumed;
                if (bodyLength < 0) {
                    throw new IllegalArgumentException("Bad packet length: " + packetLength + " (id consumed " + idBytesConsumed + ")");
                }
                state = ParseState.BODY;
            }
            case BODY -> {
                if (in.readableBytes() < bodyLength) return;
                ByteBuf body = in.readBytes(bodyLength);
                try {
                    Packet<?> packet = registry.decode(packetId, body);
                    out.add(packet);
                } finally {
                    body.release();
                }
                resetState();
            }
        }
    }

    private static int varIntSize(int value) {
        int size = 1;
        value >>>= 7;
        while (value != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }

    private static int readVarInt(ByteBuf in) {
        int out = 0;
        int bytes = 0;
        int b;
        while (bytes < 5) {
            if (!in.isReadable()) throw new IllegalArgumentException("Unexpected end of stream reading VarInt");
            b = in.readByte() & 0xFF;
            out |= (b & 0x7F) << (7 * bytes);
            bytes++;
            if ((b & 0x80) == 0) break;
        }
        return out;
    }

    private void resetState() {
        state = ParseState.LENGTH;
        packetLength = 0;
        packetId = 0;
    }
}
