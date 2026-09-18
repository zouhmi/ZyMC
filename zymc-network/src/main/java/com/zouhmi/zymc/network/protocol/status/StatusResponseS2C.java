package com.zouhmi.zymc.network.protocol.status;

import com.zouhmi.zymc.network.protocol.Packet;

public final class StatusResponseS2C implements Packet<StatusResponseS2C.StatusListener> {
    private final String json;

    public StatusResponseS2C(String json) {
        this.json = json;
    }

    public String json() {
        return json;
    }

    @Override
    public void handle(StatusListener listener) {
        listener.handle(this);
    }

    public interface StatusListener {
        void handle(StatusResponseS2C response);
    }
}
