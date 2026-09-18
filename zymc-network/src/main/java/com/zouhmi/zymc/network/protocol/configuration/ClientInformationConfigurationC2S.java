package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record ClientInformationConfigurationC2S(String locale, int viewDistance, int chatMode, boolean chatColors, int displayedSkinParts, int mainHand, boolean enableTextFiltering, boolean allowServerListings) implements Packet<ClientInformationConfigurationC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onClientInformationConfiguration(this);
    }

    public interface Listener {
        void onClientInformationConfiguration(ClientInformationConfigurationC2S packet);
    }
}
