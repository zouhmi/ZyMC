package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record PlayerPositionAndLookC2S(double x, double y, double z,
                                       float yaw, float pitch,
                                       byte flags,
                                       int teleportId) implements Packet<PlayerPositionAndLookC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPlayerPositionAndLook(this);
    }

    public interface Listener {
        void onPlayerPositionAndLook(PlayerPositionAndLookC2S packet);
    }
}
