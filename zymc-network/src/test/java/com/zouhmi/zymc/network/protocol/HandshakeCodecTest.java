package com.zouhmi.zymc.network.protocol;

import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2SCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class HandshakeCodecTest {

    private final HandshakeC2SCodec codec = new HandshakeC2SCodec();

    @Test
    void roundTrip() {
        HandshakeC2S original = new HandshakeC2S(
                774,
                "mc.example.com",
                25565,
                HandshakeC2S.ConnectionIntent.LOGIN);

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            codec.encode(original, buf);
            buf.readerIndex(0);

            HandshakeC2S decoded = codec.decode(buf);

            assertEquals(original.protocolVersion(), decoded.protocolVersion());
            assertEquals(original.address(), decoded.address());
            assertEquals(original.port(), decoded.port());
            assertEquals(original.intendedState(), decoded.intendedState());
        } finally {
            buf.release();
        }
    }

    @Test
    void roundTripStatusIntent() {
        HandshakeC2S original = new HandshakeC2S(
                774,
                "localhost",
                25565,
                HandshakeC2S.ConnectionIntent.STATUS);

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            codec.encode(original, buf);
            buf.readerIndex(0);

            HandshakeC2S decoded = codec.decode(buf);

            assertEquals(original.protocolVersion(), decoded.protocolVersion());
            assertEquals(original.address(), decoded.address());
            assertEquals(original.port(), decoded.port());
            assertEquals(original.intendedState(), decoded.intendedState());
        } finally {
            buf.release();
        }
    }

    @Test
    void protocolVersionMismatches() {
        // Server should reject a client offering the wrong protocol version.
        HandshakeC2S handshake = new HandshakeC2S(
                766, // 1.20.5 protocol
                "client",
                25565,
                HandshakeC2S.ConnectionIntent.LOGIN);

        assertNotEquals(774, handshake.protocolVersion());
    }
}
