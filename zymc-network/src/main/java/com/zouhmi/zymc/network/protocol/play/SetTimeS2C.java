package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record SetTimeS2C(long worldAge, long timeOfDay, boolean timeLocked) implements Packet<SetTimeS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onSetTime(this);
    }

    public interface Listener {
        void onSetTime(SetTimeS2C packet);
    }
}
