package com.zouhmi.zymc.network.protocol.compression;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import java.util.zip.Inflater;

public final class PacketDecompressor extends ByteToMessageDecoder {

    private enum State {
        LENGTH,
        DATA_LENGTH,
        DATA
    }

    private State state = State.LENGTH;
    private int packetLength;
    private int dataLength;

    public PacketDecompressor(int threshold) {
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
                state = State.DATA_LENGTH;
            }
            case DATA_LENGTH -> {
                if (!in.isReadable()) return;
                dataLength = ByteBufVarInts.readVarInt(in);
                if (dataLength < 0) {
                    throw new IllegalArgumentException("Bad data length: " + dataLength);
                }
                state = State.DATA;
            }
            case DATA -> {
                if (dataLength == 0) {
                    int remaining = packetLength - ByteBufVarInts.varIntSize(dataLength);
                    if (in.readableBytes() < remaining) return;
                    ByteBuf uncompressed = in.readSlice(remaining);
                    out.add(uncompressed.retain());
                    resetState();
                } else {
                    int remaining = packetLength - ByteBufVarInts.varIntSize(dataLength);
                    if (in.readableBytes() < remaining) return;
                    ByteBuf compressed = in.readSlice(remaining);
                    byte[] compressedData = new byte[compressed.readableBytes()];
                    compressed.readBytes(compressedData);

                    Inflater inflater = new Inflater();
                    try {
                        inflater.setInput(compressedData);
                        byte[] decompressed = new byte[dataLength];
                        int decompressedLen = inflater.inflate(decompressed);
                        if (decompressedLen != dataLength) {
                            throw new IllegalStateException("Decompressed size mismatch: expected " + dataLength + ", got " + decompressedLen);
                        }
                        out.add(ctx.alloc().buffer().writeBytes(decompressed));
                    } finally {
                        inflater.end();
                    }
                    resetState();
                }
            }
        }
    }

    private void resetState() {
        state = State.LENGTH;
        packetLength = 0;
        dataLength = 0;
    }
}
