package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.network.protocol.Packet;

public final class LoginHelloS2C implements Packet<LoginHelloS2C.ClientLoginListener> {
    private final String serverId;
    private final byte[] publicKeyDer;
    private final byte[] nonce;
@Override
    public void handle(LoginHelloS2C.ClientLoginListener listener) {
        listener.handle(this);
    }

    private final boolean needsAuthentication;

    public LoginHelloS2C(String serverId, byte[] publicKeyDer, byte[] nonce, boolean needsAuthentication) {
        this.serverId = serverId;
        this.publicKeyDer = publicKeyDer;
        this.nonce = nonce;
        this.needsAuthentication = needsAuthentication;
    }

    public String serverId() {
        return serverId;
    }

    public byte[] publicKeyDer() {
        return publicKeyDer;
    }

    public byte[] nonce() {
        return nonce;
    }

    public boolean needsAuthentication() {
        return needsAuthentication;
    }

    public interface ClientLoginListener {
        void handle(LoginHelloS2C hello);
    }
}
