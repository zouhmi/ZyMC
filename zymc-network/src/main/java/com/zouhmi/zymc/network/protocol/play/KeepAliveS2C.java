package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record KeepAliveS2C(long id) implements Packet<KeepAliveS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onKeepAlive(this);
    }

    public interface Listener {
        void onKeepAlive(KeepAliveS2C packet);
    }
}
