package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.core.buffer.ByteWriter;
import com.zouhmi.zymc.core.buffer.VarInts;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
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
    private ConnectionRegistry connectionRegistry;
    private final AtomicReference<HandshakeC2S> observedHandshake = new AtomicReference<>();
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        connectionRegistry = new ConnectionRegistry();
        connectionRegistry.register(ConnectionState.HANDSHAKING, 0x00, HandshakeC2S.class, new HandshakeC2SCodec());

        AtomicReference<HandshakeC2S> handshakeRef = observedHandshake;

        MinecraftServerChannelInitializer initializer = new MinecraftServerChannelInitializer(
                connectionRegistry, null, msg -> {},
                null,
                handshake -> handshakeRef.set(handshake),
                null, null);
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

        awaitState(ConnectionState.LOGIN, 200);
        assertEquals(ConnectionState.LOGIN, connectionRegistry.currentState());
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

        awaitState(ConnectionState.HANDSHAKING, 200);
        assertEquals(ConnectionState.HANDSHAKING, connectionRegistry.currentState());
    }

    @Test
    void transitionsToStatusForStatusIntent() throws Exception {
        HandshakeC2S clientHandshake = new HandshakeC2S(
                ZMCVersion.PROTOCOL_VERSION,
                "127.0.0.1",
                port,
                HandshakeC2S.ConnectionIntent.STATUS);

        clientWrite(clientHandshake);

        awaitState(ConnectionState.STATUS, 200);
        assertEquals(ConnectionState.STATUS, connectionRegistry.currentState());
    }

    private void clientWrite(HandshakeC2S packet) throws IOException {
        byte[] framed = framePacket(packet);
        Socket client = new Socket("127.0.0.1", port);
        client.getOutputStream().write(framed);
        client.getOutputStream().flush();
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        client.close();
    }

    private void awaitState(ConnectionState expected, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (connectionRegistry.currentState() == expected) return;
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
