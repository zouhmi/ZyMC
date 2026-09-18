package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record ClientInformationC2S(String locale, int viewDistance, int chatMode, boolean chatColors, int displayedSkinParts, int mainHand, boolean enableTextFiltering, boolean allowServerListings) implements Packet<ClientInformationC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onClientInformation(this);
    }

    public interface Listener {
        void onClientInformation(ClientInformationC2S packet);
    }
}
