package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record KeepAliveC2S(long id) implements Packet<KeepAliveC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onKeepAlive(this);
    }

    public interface Listener {
        void onKeepAlive(KeepAliveC2S packet);
    }
}
