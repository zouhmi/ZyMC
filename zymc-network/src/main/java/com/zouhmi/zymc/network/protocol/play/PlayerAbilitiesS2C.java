package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record PlayerAbilitiesS2C(byte flags, float flySpeed, float fov) implements Packet<PlayerAbilitiesS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPlayerAbilities(this);
    }

    public interface Listener {
        void onPlayerAbilities(PlayerAbilitiesS2C packet);
    }
}
