package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record KnownPacksS2C(String namespace, String id, String version) implements Packet<KnownPacksS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onKnownPacks(this);
    }

    public interface Listener {
        void onKnownPacks(KnownPacksS2C packet);
    }
}
