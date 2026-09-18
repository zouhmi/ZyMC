package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record ChatMessageC2S(String message) implements Packet<ChatMessageC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onChatMessage(this);
    }

    public interface Listener {
        void onChatMessage(ChatMessageC2S packet);
    }
}
