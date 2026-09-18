package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record ClientCommandC2S(int actionId) implements Packet<ClientCommandC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onClientCommand(this);
    }

    public interface Listener {
        void onClientCommand(ClientCommandC2S packet);
    }
}
