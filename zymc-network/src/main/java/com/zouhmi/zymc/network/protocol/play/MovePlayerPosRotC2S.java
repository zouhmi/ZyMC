package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record MovePlayerPosRotC2S(boolean onGround, double x, double y, double z, float yaw, float pitch) implements Packet<MovePlayerPosRotC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onMovePlayerPosRot(this);
    }

    public interface Listener {
        void onMovePlayerPosRot(MovePlayerPosRotC2S packet);
    }
}
