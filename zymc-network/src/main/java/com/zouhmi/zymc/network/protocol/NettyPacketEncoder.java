package com.zouhmi.zymc.network.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>> {

    private final ConnectionRegistry connectionRegistry;

    public NettyPacketEncoder(ConnectionRegistry connectionRegistry) {
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf out) throws Exception {
        ConnectionState currentState = ctx.channel().attr(ConnectionRegistry.STATE_KEY).get();
        if (currentState == null) currentState = ConnectionState.HANDSHAKING;
        PacketRegistry registry = connectionRegistry.registryFor(currentState);
        ByteBuf buf = ctx.alloc().buffer();
        try {
            registry.encode(packet, buf);
            int packetId = registry.packetId(packet);
            int idLength = ByteBufVarInts.varIntSize(packetId);
            int bodyLength = buf.readableBytes();
            int totalLength = idLength + bodyLength;

            ByteBufVarInts.writeVarInt(out, totalLength);
            ByteBufVarInts.writeVarInt(out, packetId);
            out.writeBytes(buf, bodyLength);
        } finally {
            buf.release();
        }
    }
}
