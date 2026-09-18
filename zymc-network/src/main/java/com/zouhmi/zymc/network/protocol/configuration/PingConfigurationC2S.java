package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record PingConfigurationC2S(int id) implements Packet<PingConfigurationC2S.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPingConfiguration(this);
    }

    public interface Listener {
        void onPingConfiguration(PingConfigurationC2S packet);
    }
}
