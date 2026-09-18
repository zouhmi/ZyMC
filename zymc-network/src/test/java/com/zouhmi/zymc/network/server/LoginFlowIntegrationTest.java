package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.crypto.RSAEngine;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.PacketRegistry;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2CCodec;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2S;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2SCodec;
import com.zouhmi.zymc.network.protocol.status.StatusResponseS2C;
import com.zouhmi.zymc.network.protocol.status.StatusResponseS2CCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.crypto.Cipher;
import javax.crypto.spec.X509EncodedKeySpec;
import static org.junit.jupiter.api.Assertions.*;

final class LoginFlowIntegrationTest {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicReference<ConnectionState> observedState = new AtomicReference<>(ConnectionState.HANDSHAKING);
    private final AtomicReference<LoginHelloS2C> receivedLoginHello = new AtomicReference<>();
    private final AtomicReference<LoginSuccessS2C> receivedLoginSuccess = new AtomicReference<>();
    private int port;
    private RSAEngine rsaEngine;

    @BeforeEach
    void startServer() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        rsaEngine = new RSAEngine();

        PacketRegistry registry = new PacketRegistry();
        registry.register(0x00, HandshakeC2S.class, new HandshakeC2SCodec());
        registry.register(0x00, StatusRequestC2S.class, new StatusRequestC2SCodec());
        registry.register(0x00, LoginHelloC2S.class, new LoginHelloC2SCodec());
        registry.register(0x01, LoginKeyC2S.class, new LoginKeyC2SCodec());
        registry.addEncoder(StatusResponseS2C.class, new StatusResponseS2CCodec(), 0x00);
        registry.addEncoder(LoginHelloS2C.class, new LoginHelloS2CCodec(), 0x00);
        registry.addEncoder(LoginSuccessS2C.class, new LoginSuccessS2CCodec(), 0x02);

        AtomicReference<LoginHelloC2S> pendingHello = new AtomicReference<>();
        AtomicReference<byte[]> sentNonce = new AtomicReference<>();

        MinecraftServerHandler handler = new MinecraftServerHandler(null, msg -> {},
                null,
                (ctx, hello) -> {
                    pendingHello.set(hello);
                    byte[] nonce = new byte[4];
                    ThreadLocalRandom.current().nextBytes(nonce);
                    sentNonce.set(nonce);
                    LoginHelloS2C response = new LoginHelloS2C(
                            "", rsaEngine.encryptPublicKeyDer(), nonce, false);
                    receivedLoginHello.set(response);
                    ctx.writeAndFlush(response);
                },
                (ctx, key) -> {
                    try {
                        rsaEngine.decryptWithPrivateKey(key.encryptedSecretKey());
                        byte[] decryptedNonce = rsaEngine.decryptWithPrivateKey(key.nonce());
                        if (!java.util.Arrays.equals(decryptedNonce, sentNonce.get())) {
                            ctx.close();
                            return;
                        }
                        LoginSuccessS2C success = new LoginSuccessS2C(
                                pendingHello.get().name(), pendingHello.get().profileId());
                        receivedLoginSuccess.set(success);
                        ctx.writeAndFlush(success);
                    } catch (Exception e) {
                        ctx.close();
                    }
                });

        MinecraftServerChannelInitializer initializer = new MinecraftServerChannelInitializer(registry, handler);
        initializer.registerHandshake();
        initializer.registerStatus();
        initializer.registerLogin();

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
        if (serverChannel != null) serverChannel.close();
        if (bossGroup != null) bossGroup.shutdownNow();
        if (workerGroup != null) workerGroup.shutdownNow();
    }

    @Test
    void loginFlowCompletes() throws Exception {
        sendHandshake(25565, HandshakeC2S.ConnectionIntent.LOGIN);
        awaitState(200);
        assertEquals(ConnectionState.LOGIN, observedState.get());

        UUID playerUuid = UUID.randomUUID();
        LoginHelloC2S loginHello = new LoginHelloC2S("TestPlayer", playerUuid);
        sendPacket(loginHello, 0x00);

        Thread.sleep(100);

        LoginHelloS2C serverHello = receivedLoginHello.get();
        assertNotNull(serverHello, "Server should send LoginHelloS2C");
        assertNotNull(serverHello.publicKeyDer());
        assertTrue(serverHello.publicKeyDer().length > 0);
        assertNotNull(serverHello.nonce());
        assertEquals(4, serverHello.nonce().length);

        byte[] sharedSecret = new byte[16];
        ThreadLocalRandom.current().nextBytes(sharedSecret);
        byte[] encryptedSecret = encryptWithPublicKey(serverHello.publicKeyDer(), sharedSecret);
        byte[] encryptedNonce = encryptWithPublicKey(serverHello.publicKeyDer(), serverHello.nonce());

        LoginKeyC2S loginKey = new LoginKeyC2S(encryptedSecret, encryptedNonce);
        sendPacket(loginKey, 0x01);

        Thread.sleep(200);

        assertEquals(ConnectionState.LOGIN, observedState.get(), "State should remain LOGIN after LoginKey (we don't auto-transition to PLAY in test)");
        assertNotNull(receivedLoginSuccess.get(), "Server should send LoginSuccess");
        assertEquals("TestPlayer", receivedLoginSuccess.get().name());
        assertEquals(playerUuid, receivedLoginSuccess.get().profileId());
    }

    private void sendHandshake(int serverPort, HandshakeC2S.ConnectionIntent intent) throws Exception {
        sendPacket(new HandshakeC2S(ZMCVersion.PROTOCOL_VERSION, "127.0.0.1", serverPort, intent), 0x00);
    }

    private void sendPacket(Object packet, int packetId) throws Exception {
        PacketRegistry registry = new PacketRegistry();
        if (packet instanceof HandshakeC2S) {
            registry.register(0x00, HandshakeC2S.class, new HandshakeC2SCodec());
        } else if (packet instanceof StatusRequestC2S) {
            registry.register(0x00, StatusRequestC2S.class, new StatusRequestC2SCodec());
        } else if (packet instanceof LoginHelloC2S) {
            registry.register(0x00, LoginHelloC2S.class, new LoginHelloC2SCodec());
        } else if (packet instanceof LoginKeyC2S) {
            registry.register(0x01, LoginKeyC2S.class, new LoginKeyC2SCodec());
        } else {
            throw new IllegalArgumentException("Unknown packet type: " + packet.getClass().getName());
        }

        ByteBuf body = ByteBufAllocator.DEFAULT.buffer();
        registry.encode((com.zouhmi.zymc.network.protocol.Packet<?>) packet, body);
        int bodyLength = body.readableBytes();
        int idLength = varIntSize(packetId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeVarInt(out, bodyLength + idLength);
        writeVarInt(out, packetId);
        byte[] bodyBytes = new byte[bodyLength];
        body.readBytes(bodyBytes);
        out.write(bodyBytes);
        body.release();
        byte[] framed = out.toByteArray();

        try (Socket sock = new Socket("127.0.0.1", port)) {
            sock.getOutputStream().write(framed);
            sock.getOutputStream().flush();
            Thread.sleep(30);
        }
    }

    private byte[] encryptWithPublicKey(byte[] publicKeyDer, byte[] data) {
        try {
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            java.security.PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyDer));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private static void writeVarInt(ByteArrayOutputStream out, int value) {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            out.write(part);
        } while (value != 0);
    }

    private void awaitState(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (observedState.get() != ConnectionState.HANDSHAKING) return;
            Thread.sleep(10);
        }
    }
}
