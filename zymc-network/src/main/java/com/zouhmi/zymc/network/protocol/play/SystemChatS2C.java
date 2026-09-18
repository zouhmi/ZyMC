package com.zouhmi.zymc.network.protocol.play;

import com.zouhmi.zymc.network.protocol.Packet;

public record SystemChatS2C(String message, boolean overlay) implements Packet<SystemChatS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onSystemChat(this);
    }

    public interface Listener {
        void onSystemChat(SystemChatS2C packet);
    }
}
