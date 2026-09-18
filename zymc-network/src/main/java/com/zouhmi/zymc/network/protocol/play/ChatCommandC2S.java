package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record ChatCommandC2S(String command) implements Packet<ChatCommandC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onChatCommand(this);
    }

    public interface Listener {
        void onChatCommand(ChatCommandC2S packet);
    }
}
