package com.zouhmi.zymc.network.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.Objects;

public final class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>> {

    private final PacketRegistry registry;

    public NettyPacketEncoder(PacketRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        try {
            registry.encode(packet, buf);
            int packetId = registry.packetId(packet);
            int idLength = varIntSize(packetId);
            int bodyLength = buf.readableBytes();
            int totalLength = idLength + bodyLength;

            writeVarInt(out, totalLength);
            writeVarInt(out, packetId);
            out.writeBytes(buf, bodyLength);
        } finally {
            buf.release();
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
