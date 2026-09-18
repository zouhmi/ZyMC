package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record PlayerRotationS2C(float yaw, float pitch, boolean onGround) implements Packet<PlayerRotationS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPlayerRotation(this);
    }

    public interface Listener {
        void onPlayerRotation(PlayerRotationS2C packet);
    }
}
