package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record MovePlayerStatusOnlyC2S(boolean onGround) implements Packet<MovePlayerStatusOnlyC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onMovePlayerStatusOnly(this);
    }

    public interface Listener {
        void onMovePlayerStatusOnly(MovePlayerStatusOnlyC2S packet);
    }
}
