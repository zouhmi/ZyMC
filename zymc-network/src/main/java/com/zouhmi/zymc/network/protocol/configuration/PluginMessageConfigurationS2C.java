package com.zouhmi.zymc.network.protocol.configuration;

import com.zouhmi.zymc.network.protocol.Packet;

public record PluginMessageConfigurationS2C(String channel, byte[] data) implements Packet<PluginMessageConfigurationS2C.Listener> {

    @Override
    public void handle(Listener listener) {
        listener.onPluginMessageConfiguration(this);
    }

    public interface Listener {
        void onPluginMessageConfiguration(PluginMessageConfigurationS2C packet);
    }
}
