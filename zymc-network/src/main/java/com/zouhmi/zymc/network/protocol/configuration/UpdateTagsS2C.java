package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record UpdateTagsS2C() implements Packet<UpdateTagsS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onUpdateTags(this);
    }

    public interface Listener {
        void onUpdateTags(UpdateTagsS2C packet);
    }
}
