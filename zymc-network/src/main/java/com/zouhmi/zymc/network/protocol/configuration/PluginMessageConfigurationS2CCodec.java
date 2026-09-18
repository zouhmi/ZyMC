package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class PluginMessageConfigurationS2CCodec implements PacketCodec<PluginMessageConfigurationS2C> {

    @Override
    public void encode(PluginMessageConfigurationS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.channel());
        buf.writeBytes(packet.data());
    }

    @Override
    public PluginMessageConfigurationS2C decode(ByteBuf buf) {
        String channel = ByteBufVarInts.readString(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new PluginMessageConfigurationS2C(channel, data);
    }
}
