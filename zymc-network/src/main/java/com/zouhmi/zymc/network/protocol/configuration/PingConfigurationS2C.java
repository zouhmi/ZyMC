package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record PingConfigurationS2C(int id) implements Packet<PingConfigurationS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPingConfiguration(this);
    }

    public interface Listener {
        void onPingConfiguration(PingConfigurationS2C packet);
    }
}
