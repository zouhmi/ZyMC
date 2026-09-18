package com.zouhmi.zymc.network.protocol.handshake;

import com.zouhmi.zymc.network.protocol.Packet;

public final class HandshakeC2S implements Packet<HandshakeC2S.HandshakeListener> {
    private int protocolVersion;
    private String address;
    private int port;
    private ConnectionIntent intendedState;

    public HandshakeC2S(int protocolVersion, String address, int port, ConnectionIntent intendedState) {
        this.protocolVersion = protocolVersion;
        this.address = address;
        this.port = port;
        this.intendedState = intendedState;
    }

    public int protocolVersion() {
        return protocolVersion;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }

    public ConnectionIntent intendedState() {
        return intendedState;
    }

    @Override
    public void handle(HandshakeListener listener) {
        listener.handle(this);
    }

    public enum ConnectionIntent {
        STATUS(1),
        LOGIN(2);

        private final int value;

        ConnectionIntent(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static ConnectionIntent fromValue(int value) {
            for (ConnectionIntent intent : values()) {
                if (intent.value() == value) return intent;
            }
            throw new IllegalArgumentException("Unknown connection intent: " + value);
        }
    }

    public interface HandshakeListener {
        void handle(HandshakeC2S handshake);
    }
}
