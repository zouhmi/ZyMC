package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record GameEventS2C(byte eventId, float value) implements Packet<GameEventS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onGameEvent(this);
    }

    public interface Listener {
        void onGameEvent(GameEventS2C packet);
    }
}
