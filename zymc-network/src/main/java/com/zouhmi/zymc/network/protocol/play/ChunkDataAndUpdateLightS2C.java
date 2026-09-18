package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record ChunkDataAndUpdateLightS2C(int chunkX, int chunkZ, byte[] chunkData) implements Packet<ChunkDataAndUpdateLightS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onChunkData(this);
    }

    public interface Listener {
        void onChunkData(ChunkDataAndUpdateLightS2C packet);
    }
}
