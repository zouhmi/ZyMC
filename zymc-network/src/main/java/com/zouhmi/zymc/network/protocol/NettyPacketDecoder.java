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
    private final ConnectionRegistry connectionRegistry;

    public NettyPacketDecoder(ConnectionRegistry connectionRegistry) {
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state) {
            case LENGTH -> {
                if (!in.isReadable()) return;
                packetLength = ByteBufVarInts.readVarInt(in);
                if (packetLength < 0) {
                    throw new IllegalArgumentException("Bad packet length: " + packetLength);
                }
                state = ParseState.ID;
            }
            case ID -> {
                if (!in.isReadable()) return;
                packetId = ByteBufVarInts.readVarInt(in);
                int idBytesConsumed = ByteBufVarInts.varIntSize(packetId);
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
                    ConnectionState currentState = ctx.channel().attr(ConnectionRegistry.STATE_KEY).get();
                    if (currentState == null) currentState = ConnectionState.HANDSHAKING;
                    PacketRegistry registry = connectionRegistry.registryFor(currentState);
                    Packet<?> packet = registry.decode(packetId, body);
                    out.add(packet);
                } finally {
                    body.release();
                }
                resetState();
            }
        }
    }

    private void resetState() {
        state = ParseState.LENGTH;
        packetLength = 0;
        packetId = 0;
    }
}
