package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.core.buffer.ByteWriter;
import com.zouhmi.zymc.core.buffer.VarInts;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.PacketRegistry;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2SCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

final class HandshakeServerIntegrationTest {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicReference<ConnectionState> observedState = new AtomicReference<>(ConnectionState.HANDSHAKING);
    private final AtomicReference<HandshakeC2S> observedHandshake = new AtomicReference<>();
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        PacketRegistry registry = new PacketRegistry();
        registry.register(0x00, HandshakeC2S.class, new HandshakeC2SCodec());

        AtomicReference<HandshakeC2S> handshakeRef = observedHandshake;
        AtomicReference<ConnectionState> stateRef = observedState;
        AtomicReference<HandshakeC2S> handshakeRef = observedHandshake;
        MinecraftServerHandler handler = new MinecraftServerHandler(
                null,
                msg -> {},
                handshake -> {
                    handshakeRef.set(handshake);
                    // state stays HANDSHAKING after handshake (we only transition on status/login intent)
                });

        MinecraftServerChannelInitializer initializer = new MinecraftServerChannelInitializer(registry, handler);
        initializer.registerHandshake();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(initializer);

        ChannelFuture f = b.bind(new InetSocketAddress("127.0.0.1", 0)).sync();
        serverChannel = f.channel();
        port = ((InetSocketAddress) serverChannel.localAddress()).getPort();
    }

    @AfterEach
    void stopServer() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) bossGroup.shutdownNow();
        if (workerGroup != null) workerGroup.shutdownNow();
    }

    @Test
    void acceptsValidHandshakeAndTransitionsToLogin() throws Exception {
        HandshakeC2S clientHandshake = new HandshakeC2S(
                ZMCVersion.PROTOCOL_VERSION,
                "127.0.0.1",
                port,
                HandshakeC2S.ConnectionIntent.LOGIN);

        clientWrite(clientHandshake);

        awaitState(200);
        assertEquals(ConnectionState.LOGIN, observedState.get());
        HandshakeC2S received = observedHandshake.get();
        assertNotNull(received);
        assertEquals(ZMCVersion.PROTOCOL_VERSION, received.protocolVersion());
        assertEquals("127.0.0.1", received.address());
        assertEquals(port, received.port());
        assertEquals(HandshakeC2S.ConnectionIntent.LOGIN, received.intendedState());
    }

    @Test
    void rejectsWrongProtocolVersion() throws Exception {
        HandshakeC2S clientHandshake = new HandshakeC2S(
                766,
                "127.0.0.1",
                port,
                HandshakeC2S.ConnectionIntent.LOGIN);

        clientWrite(clientHandshake);

        awaitState(200);
        assertEquals(ConnectionState.HANDSHAKING, observedState.get());
    }

    @Test
    void transitionsToStatusForStatusIntent() throws Exception {
        HandshakeC2S clientHandshake = new HandshakeC2S(
                ZMCVersion.PROTOCOL_VERSION,
                "127.0.0.1",
                port,
                HandshakeC2S.ConnectionIntent.STATUS);

        clientWrite(clientHandshake);

        awaitState(200);
        assertEquals(ConnectionState.STATUS, observedState.get());
    }

    private void clientWrite(HandshakeC2S packet) throws IOException {
        byte[] framed = framePacket(packet);
        Socket client = new Socket("127.0.0.1", port);
        client.getOutputStream().write(framed);
        client.getOutputStream().flush();
        // Keep the socket open briefly so the server's event loop can process the packet
        // before the client closes the connection.
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        client.close();
    }

    private void awaitState(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (observedState.get() != ConnectionState.HANDSHAKING) return;
            Thread.sleep(10);
        }
    }

    private static byte[] framePacket(HandshakeC2S packet) throws IOException {
        PacketRegistry registry = new PacketRegistry();
        registry.register(0x00, HandshakeC2S.class, new HandshakeC2SCodec());

        ByteBuf body = ByteBufAllocator.DEFAULT.buffer();
        try {
            registry.encode(packet, body);
            int packetId = registry.packetId(packet);
            int idLength = varIntSize(packetId);
            int bodyLength = body.readableBytes();
            int totalLength = idLength + bodyLength;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            VarInts.writeVarInt(byteWriterFrom(out), totalLength);
            VarInts.writeVarInt(byteWriterFrom(out), packetId);
            body.readBytes(out, bodyLength);
            return out.toByteArray();
        } finally {
            body.release();
        }
    }

    private static int varIntSize(int value) {
        int size = 1;
        value >>>= 7;
        while (value != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }

    private static ByteWriter byteWriterFrom(ByteArrayOutputStream out) {
        return value -> out.write(value & 0xFF);
    }
}
