package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record RegistryDataS2C(String registryId, String[] data) implements Packet<RegistryDataS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onRegistryData(this);
    }

    public interface Listener {
        void onRegistryData(RegistryDataS2C packet);
    }
}
