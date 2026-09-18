package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record SpawnPositionS2C(double x, double y, double z, float angle) implements Packet<SpawnPositionS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onSpawnPosition(this);
    }

    public interface Listener {
        void onSpawnPosition(SpawnPositionS2C packet);
    }
}
