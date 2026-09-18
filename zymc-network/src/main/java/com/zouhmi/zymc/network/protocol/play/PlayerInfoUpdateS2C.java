package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;
import java.util.UUID;

public record PlayerInfoUpdateS2C(byte action, UUID[] uuids) implements Packet<PlayerInfoUpdateS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPlayerInfoUpdate(this);
    }

    public interface Listener {
        void onPlayerInfoUpdate(PlayerInfoUpdateS2C packet);
    }
}
