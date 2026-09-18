package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record PlayerPositionAndLookS2C(double x, double y, double z,
                                       float yaw, float pitch,
                                       byte flags,
                                       int teleportId) implements Packet<PlayerPositionAndLookS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPlayerPositionAndLook(this);
    }

    public interface Listener {
        void onPlayerPositionAndLook(PlayerPositionAndLookS2C packet);
    }
}
