package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.ByteBufVarInts;
import com.zouhmi.zymc.network.protocol.PacketCodec;
import io.netty.buffer.ByteBuf;

public final class ClientInformationC2SCodec implements PacketCodec<ClientInformationC2S> {

    @Override
    public void encode(ClientInformationC2S packet, ByteBuf buf) {
        ByteBufVarInts.writeString(buf, packet.locale());
        ByteBufVarInts.writeVarInt(buf, packet.viewDistance());
        ByteBufVarInts.writeVarInt(buf, packet.chatMode());
        ByteBufVarInts.writeBoolean(buf, packet.chatColors());
        ByteBufVarInts.writeVarInt(buf, packet.displayedSkinParts());
        ByteBufVarInts.writeVarInt(buf, packet.mainHand());
        ByteBufVarInts.writeBoolean(buf, packet.enableTextFiltering());
        ByteBufVarInts.writeBoolean(buf, packet.allowServerListings());
    }

    @Override
    public ClientInformationC2S decode(ByteBuf buf) {
        String locale = ByteBufVarInts.readString(buf);
        int viewDistance = ByteBufVarInts.readVarInt(buf);
        int chatMode = ByteBufVarInts.readVarInt(buf);
        boolean chatColors = ByteBufVarInts.readBoolean(buf);
        int displayedSkinParts = ByteBufVarInts.readVarInt(buf);
        int mainHand = ByteBufVarInts.readVarInt(buf);
        boolean enableTextFiltering = ByteBufVarInts.readBoolean(buf);
        boolean allowServerListings = ByteBufVarInts.readBoolean(buf);
        return new ClientInformationC2S(locale, viewDistance, chatMode, chatColors, displayedSkinParts, mainHand, enableTextFiltering, allowServerListings);
    }
}
