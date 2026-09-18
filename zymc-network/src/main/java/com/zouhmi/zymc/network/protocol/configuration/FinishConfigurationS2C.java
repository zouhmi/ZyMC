package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record FinishConfigurationS2C() implements Packet<FinishConfigurationS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onFinishConfiguration(this);
    }

    public interface Listener {
        void onFinishConfiguration(FinishConfigurationS2C packet);
    }
}
