package com.zouhmi.zymc.network.protocol.compression;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.Packet;
import com.zouhmi.zymc.network.protocol.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public final class PacketCompressor extends MessageToByteEncoder<Packet<?>> {

    private final int threshold;
    private final ConnectionRegistry connectionRegistry;

    public PacketCompressor(int threshold, ConnectionRegistry connectionRegistry) {
        this.threshold = threshold;
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf out) throws Exception {
        PacketRegistry registry = connectionRegistry.currentRegistry();
        ByteBuf buf = ctx.alloc().buffer();
        try {
            registry.encode(packet, buf);
            int packetId = registry.packetId(packet);
            int idLength = ByteBufVarInts.varIntSize(packetId);
            int dataLen = idLength + buf.readableBytes();

            if (dataLen < threshold) {
                ByteBufVarInts.writeVarInt(out, 1 + dataLen);
                ByteBufVarInts.writeVarInt(out, 0);
                ByteBufVarInts.writeVarInt(out, packetId);
                out.writeBytes(buf, buf.readableBytes());
            } else {
                ByteBuf uncompressed = ctx.alloc().buffer();
                try {
                    ByteBufVarInts.writeVarInt(uncompressed, packetId);
                    uncompressed.writeBytes(buf, buf.readableBytes());
                    byte[] input = new byte[uncompressed.readableBytes()];
                    uncompressed.readBytes(input);

                    Deflater deflater = new Deflater();
                    try {
                        deflater.setInput(input);
                        deflater.finish();
                        byte[] compressed = new byte[input.length];
                        int compressedLen = deflater.deflate(compressed);

                        int dataLengthSize = ByteBufVarInts.varIntSize(dataLen);
                        ByteBufVarInts.writeVarInt(out, dataLengthSize + compressedLen);
                        ByteBufVarInts.writeVarInt(out, dataLen);
                        out.writeBytes(compressed, 0, compressedLen);
                    } finally {
                        deflater.end();
                    }
                } finally {
                    uncompressed.release();
                }
            }
        } finally {
            buf.release();
        }
    }
}
