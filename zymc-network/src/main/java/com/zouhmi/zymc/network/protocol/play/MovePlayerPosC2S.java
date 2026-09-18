package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record MovePlayerPosC2S(boolean onGround, double x, double y, double z) implements Packet<MovePlayerPosC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onMovePlayerPos(this);
    }

    public interface Listener {
        void onMovePlayerPos(MovePlayerPosC2S packet);
    }
}
