package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record SetCenterChunkS2C(int chunkX, int chunkZ) implements Packet<SetCenterChunkS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onSetCenterChunk(this);
    }

    public interface Listener {
        void onSetCenterChunk(SetCenterChunkS2C packet);
    }
}
