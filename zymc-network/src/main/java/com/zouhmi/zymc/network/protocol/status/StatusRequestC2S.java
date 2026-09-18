package com.zouhmi.zymc.network.protocol.status;

import com.zouhmi.zymc.network.protocol.Packet;

public final class StatusRequestC2S implements Packet<StatusRequestC2S.StatusStateListener> {
    @Override
    public void handle(StatusStateListener listener) {
        listener.handle(this);
    }

    public interface StatusStateListener {
        void handle(StatusRequestC2S request);
    }
}
