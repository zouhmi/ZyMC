package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record AcceptTeleportationC2S(int teleportId) implements Packet<AcceptTeleportationC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onAcceptTeleportation(this);
    }

    public interface Listener {
        void onAcceptTeleportation(AcceptTeleportationC2S packet);
    }
}
