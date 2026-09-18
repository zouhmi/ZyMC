package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.network.protocol.Packet;

public final class LoginSuccessS2C implements Packet<LoginSuccessS2C.ClientLoginListener> {
    private final String name;
    private final java.util.UUID profileId;

    public LoginSuccessS2C(String name, java.util.UUID profileId) {
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
    public void handle(ClientLoginListener listener) {
        listener.handle(this);
    }

    public interface ClientLoginListener {
        void handle(LoginSuccessS2C success);
    }
}
