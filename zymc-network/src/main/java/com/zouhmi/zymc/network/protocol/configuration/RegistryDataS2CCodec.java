package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class RegistryDataS2CCodec implements PacketCodec<RegistryDataS2C> {

    @Override
    public void encode(RegistryDataS2C packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.registryId());
        ByteBufVarInts.writeVarInt(buf, packet.data().length);
        for (String entry : packet.data()) {
            ByteBufVarInts.writeString(buf, entry);
        }
    }

    @Override
    public RegistryDataS2C decode(ByteBuf buf) {
        String registryId = ByteBufVarInts.readString(buf);
        int count = ByteBufVarInts.readVarInt(buf);
        String[] data = new String[count];
        for (int i = 0; i < count; i++) {
            data[i] = ByteBufVarInts.readString(buf);
        }
        return new RegistryDataS2C(registryId, data);
    }
}
