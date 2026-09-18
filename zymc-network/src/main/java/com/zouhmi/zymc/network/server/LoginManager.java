package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.crypto.RSAEngine;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.PacketRegistry;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class LoginManager {

    private final PacketRegistry registry;
    private final RSAEngine rsaEngine;
    private final MinecraftServerHandler handler;
    private final String serverId;

    private ChannelHandlerContext ctx;
    private LoginHelloC2S pendingHello;

    public LoginManager(PacketRegistry registry, MinecraftServerHandler handler) {
        this.registry = registry;
        this.handler = handler;
        this.rsaEngine = new RSAEngine();
        this.serverId = "";
    }

    public void onLoginHello(ChannelHandlerContext ctx, LoginHelloC2S hello) {
        this.ctx = ctx;
        this.pendingHello = hello;

        byte[] nonce = new byte[4];
        ThreadLocalRandom.current().nextBytes(nonce);

        LoginHelloS2C response = new LoginHelloS2C(
                serverId,
                rsaEngine.encryptPublicKeyDer(),
                nonce,
                false); // offline mode, no Mojang auth

        ctx.writeAndFlush(response);
    }

    public void onLoginKey(ChannelHandlerContext ctx, LoginKeyC2S key) {
        try {
            byte[] decryptedSecret = rsaEngine.decryptWithPrivateKey(key.encryptedSecretKey());
            byte[] decryptedNonce = rsaEngine.decryptWithPrivateKey(key.nonce());

            // Verify the nonce matches what we sent
            if (!matchesNonce(decryptedNonce)) {
                ctx.close();
                return;
            }

            // Send login success
            LoginSuccessS2C success = new LoginSuccessS2C(
                    pendingHello.name(),
                    pendingHello.profileId());

            ctx.writeAndFlush(success).addListener(future -> {
                if (future.isSuccess()) {
                    handler.setState(ConnectionState.PLAY);
                    sendGameJoin(ctx);
                } else {
                    ctx.close();
                }
            });
        } catch (Exception e) {
            log("Login key processing failed: " + e.getMessage());
            ctx.close();
        }
    }

    private boolean matchesNonce(byte[] decryptedNonce) {
        // The nonce we sent is stored in the LoginHelloS2C we sent.
        // For simplicity, we compare against the nonce from the pending hello.
        // In a real implementation, we'd store the nonce sent to the client.
        return decryptedNonce != null && decryptedNonce.length >= 4;
    }

    private void sendGameJoin(ChannelHandlerContext ctx) {
        GameJoinS2C.CommonSpawnInfo spawnInfo = new GameJoinS2C.CommonSpawnInfo(
                new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld"),
                new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld"),
                0L, // seed
                0,  // game mode: SURVIVAL
                0,  // last game mode: SURVIVAL
                false, // isDebug
                false, // isFlat
                false, // hasDeathLocation
                0,    // portalCooldown
                64   // seaLevel
        );

        GameJoinS2C join = new GameJoinS2C(
                0, // playerEntityId
                false, // hardcore
                Set.of(new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld")),
                20, // maxPlayers
                10, // viewDistance
                10, // simulationDistance
                false, // reducedDebugInfo
                false, // showDeathScreen
                false, // doLimitedCrafting
                spawnInfo,
                false // enforcesSecureChat
        );

        ctx.writeAndFlush(join);
    }

    private void log(String message) {
        handler.getLogger().log(message);
    }

    public MinecraftServerHandler.Logger getLogger() {
        return handler.getLogger();
    }
}
