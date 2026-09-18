package com.zouhmi.zymc.benchmark.client;

import com.zouhmi.zymc.benchmark.metrics.ClientMetrics;
import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.crypto.RSAEngine;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.Packet;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationC2S;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationS2C;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2S;
import com.zouhmi.zymc.network.protocol.play.ChunkDataAndUpdateLightS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import com.zouhmi.zymc.network.protocol.play.KeepAliveC2S;
import com.zouhmi.zymc.network.protocol.play.KeepAliveS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookS2C;
import com.zouhmi.zymc.network.protocol.play.SynchronizePlayerPositionS2C;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

public final class BenchmarkClientHandler extends SimpleChannelInboundHandler<Packet<?>> {

    private final ClientMetrics metrics;
    private final ConnectionRegistry connectionRegistry;
    private final UUID playerUuid;
    private final String playerName;
    private final RSAEngine rsaEngine = new RSAEngine();
    private byte[] sharedSecret;
    private long lastKeepAliveSendTime;

    public BenchmarkClientHandler(ClientMetrics metrics, ConnectionRegistry connectionRegistry,
                                   UUID playerUuid, String playerName) {
        this.metrics = metrics;
        this.connectionRegistry = connectionRegistry;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        metrics.setConnectStartTime(System.currentTimeMillis());
        ctx.channel().attr(ConnectionRegistry.STATE_KEY).set(ConnectionState.HANDSHAKING);

        HandshakeC2S handshake = new HandshakeC2S(
                ZMCVersion.PROTOCOL_VERSION, "localhost", 25565,
                HandshakeC2S.ConnectionIntent.LOGIN);
        ctx.writeAndFlush(handshake);

        ctx.channel().attr(ConnectionRegistry.STATE_KEY).set(ConnectionState.LOGIN);

        LoginHelloC2S hello = new LoginHelloC2S(playerName, playerUuid);
        ctx.writeAndFlush(hello);
        metrics.setHandshakeSendTime(System.currentTimeMillis());
        metrics.recordPacketSent(0);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        metrics.setConnected(false);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        metrics.setFailed(true);
        metrics.setFailureReason(cause.getMessage());
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) throws Exception {
        metrics.recordPacketReceived(0);
        ConnectionState state = ctx.channel().attr(ConnectionRegistry.STATE_KEY).get();
        if (state == null) state = ConnectionState.HANDSHAKING;

        switch (state) {
            case LOGIN -> handleLogin(ctx, packet);
            case CONFIGURATION -> handleConfiguration(ctx, packet);
            case PLAY -> handlePlay(ctx, packet);
            default -> {}
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, Packet<?> packet) throws Exception {
        if (packet instanceof LoginHelloS2C hello) {
            metrics.setLoginHelloRecvTime(System.currentTimeMillis());
            metrics.setLoginKeySendTime(System.currentTimeMillis());

            java.security.PublicKey serverKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(hello.publicKeyDer()));

            sharedSecret = new byte[16];
            new java.security.SecureRandom().nextBytes(sharedSecret);

            byte[] encryptedSecret = rsaEngine.encryptWithPublicKey(serverKey, sharedSecret);
            byte[] encryptedNonce = rsaEngine.encryptWithPublicKey(serverKey, hello.nonce());

            LoginKeyC2S key = new LoginKeyC2S(encryptedSecret, encryptedNonce);
            ctx.writeAndFlush(key);
            metrics.recordPacketSent(0);

        } else if (packet instanceof LoginSuccessS2C) {
            metrics.setLoginSuccessRecvTime(System.currentTimeMillis());

            ctx.channel().attr(ConnectionRegistry.STATE_KEY).set(ConnectionState.CONFIGURATION);
            metrics.setConfigurationStartTime(System.currentTimeMillis());

            enableEncryption(ctx);
        }
    }

    private void enableEncryption(ChannelHandlerContext ctx) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

        Cipher encCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        encCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        Cipher decCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        decCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        ctx.pipeline().addBefore("packet-decoder", "cipher-dec", new BenchmarkCipherDecoder(decCipher));
        ctx.pipeline().addAfter("packet-encoder", "cipher-enc", new BenchmarkCipherEncoder(encCipher));
    }

    private void handleConfiguration(ChannelHandlerContext ctx, Packet<?> packet) {
        if (packet instanceof FinishConfigurationS2C) {
            ctx.channel().attr(ConnectionRegistry.STATE_KEY).set(ConnectionState.PLAY);
            FinishConfigurationC2S finish = new FinishConfigurationC2S();
            ctx.writeAndFlush(finish);
            metrics.setConfigurationFinishTime(System.currentTimeMillis());
            metrics.recordPacketSent(0);
        }
    }

    private void handlePlay(ChannelHandlerContext ctx, Packet<?> packet) {
        if (packet instanceof GameJoinS2C) {
            metrics.setPlayStartTime(System.currentTimeMillis());
        } else if (packet instanceof KeepAliveS2C keepAlive) {
            lastKeepAliveSendTime = System.nanoTime();
            KeepAliveC2S response = new KeepAliveC2S(keepAlive.id());
            ctx.writeAndFlush(response);
            metrics.recordPacketSent(0);
        } else if (packet instanceof PlayerPositionAndLookS2C pos) {
            AcceptTeleportationC2S ack = new AcceptTeleportationC2S(pos.teleportId());
            ctx.writeAndFlush(ack);
            metrics.recordPacketSent(0);
        } else if (packet instanceof SynchronizePlayerPositionS2C pos) {
            AcceptTeleportationC2S ack = new AcceptTeleportationC2S(pos.teleportId());
            ctx.writeAndFlush(ack);
            metrics.recordPacketSent(0);
        } else if (packet instanceof ChunkDataAndUpdateLightS2C) {
            metrics.recordChunkReceived();
            if (metrics.getChunksReceived() == 1) {
                metrics.setFirstChunkRecvTime(System.currentTimeMillis());
            }
        }
    }
}
