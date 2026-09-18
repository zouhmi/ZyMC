package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record SynchronizePlayerPositionS2C(double x, double y, double z,
                                           float yaw, float pitch,
                                           byte flags,
                                           int teleportId) implements Packet<SynchronizePlayerPositionS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onSynchronizePlayerPosition(this);
    }

    public interface Listener {
        void onSynchronizePlayerPosition(SynchronizePlayerPositionS2C packet);
    }
}
