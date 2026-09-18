package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record MovePlayerRotC2S(boolean onGround, float yaw, float pitch) implements Packet<MovePlayerRotC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onMovePlayerRot(this);
    }

    public interface Listener {
        void onMovePlayerRot(MovePlayerRotC2S packet);
    }
}
