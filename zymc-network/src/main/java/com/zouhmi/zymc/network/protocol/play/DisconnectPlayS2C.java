package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record DisconnectPlayS2C(String reason) implements Packet<DisconnectPlayS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onDisconnectPlay(this);
    }

    public interface Listener {
        void onDisconnectPlay(DisconnectPlayS2C packet);
    }
}
