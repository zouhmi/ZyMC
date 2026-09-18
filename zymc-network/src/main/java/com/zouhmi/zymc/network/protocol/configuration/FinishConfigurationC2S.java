package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record FinishConfigurationC2S() implements Packet<FinishConfigurationC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onFinishConfiguration(this);
    }

    public interface Listener {
        void onFinishConfiguration(FinishConfigurationC2S packet);
    }
}
