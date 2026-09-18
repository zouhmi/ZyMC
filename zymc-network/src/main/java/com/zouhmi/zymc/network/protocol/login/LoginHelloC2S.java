package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.network.protocol.Packet;

public final class LoginHelloC2S implements Packet<LoginHelloC2S.ServerLoginListener> {
    private final String name;
    private final java.util.UUID profileId;

    public LoginHelloC2S(String name, java.util.UUID profileId) {
        this.name = name;
        this.profileId = profileId;
    }

    public String name() {
        return name;
    }

    public java.util.UUID profileId() {
        return profileId;
    }

    @Override
    public void handle(ServerLoginListener listener) {
        listener.handle(this);
    }

    public interface ServerLoginListener {
        void handle(LoginHelloC2S hello);
    }
}
