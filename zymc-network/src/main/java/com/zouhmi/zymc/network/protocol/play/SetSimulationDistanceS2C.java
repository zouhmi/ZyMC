package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record SetSimulationDistanceS2C(int distance) implements Packet<SetSimulationDistanceS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onSetSimulationDistance(this);
    }

    public interface Listener {
        void onSetSimulationDistance(SetSimulationDistanceS2C packet);
    }
}
