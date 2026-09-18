package com.zouhmi.zymc.network.protocol.login;

import com.zouhmi.zymc.network.protocol.Packet;

public final class LoginKeyC2S implements Packet<LoginKeyC2S.ServerLoginListener> {
    private final byte[] encryptedSecretKey;
    private final byte[] nonce;

    public LoginKeyC2S(byte[] encryptedSecretKey, byte[] nonce) {
        this.encryptedSecretKey = encryptedSecretKey;
        this.nonce = nonce;
    }

    public byte[] encryptedSecretKey() {
        return encryptedSecretKey;
    }

    public byte[] nonce() {
        return nonce;
    }

    @Override
    public void handle(ServerLoginListener listener) {
        listener.handle(this);
    }

    public interface ServerLoginListener {
        void handle(LoginHelloC2S hello);
        void handle(LoginKeyC2S key);
    }
}
