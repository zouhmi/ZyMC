package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.crypto.RSAEngine;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.play.GameEventS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerInfoUpdateS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookS2C;
import com.zouhmi.zymc.network.protocol.play.SetCenterChunkS2C;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class LoginManager {

    private final ConnectionRegistry connectionRegistry;
    private final RSAEngine rsaEngine;
    private final MinecraftServerHandler handler;
    private final String serverId;

    private ChannelHandlerContext ctx;
    private LoginHelloC2S pendingHello;
    private byte[] sentNonce;

    public LoginManager(ConnectionRegistry connectionRegistry, MinecraftServerHandler handler) {
        this.connectionRegistry = connectionRegistry;
        this.handler = handler;
        this.rsaEngine = new RSAEngine();
        this.serverId = "";
    }

    public void onLoginHello(ChannelHandlerContext ctx, LoginHelloC2S hello) {
        this.ctx = ctx;
        this.pendingHello = hello;

        byte[] nonce = new byte[4];
        ThreadLocalRandom.current().nextBytes(nonce);
        this.sentNonce = nonce;

        LoginHelloS2C response = new LoginHelloS2C(
                serverId,
                rsaEngine.encryptPublicKeyDer(),
                nonce,
                false);

        ctx.writeAndFlush(response);
    }

    public void onLoginKey(ChannelHandlerContext ctx, LoginKeyC2S key) {
        try {
            byte[] decryptedSecret = rsaEngine.decryptWithPrivateKey(key.encryptedSecretKey());
            byte[] decryptedNonce = rsaEngine.decryptWithPrivateKey(key.nonce());

            if (!matchesNonce(decryptedNonce)) {
                ctx.close();
                return;
            }

            LoginSuccessS2C success = new LoginSuccessS2C(
                    pendingHello.name(),
                    pendingHello.profileId());

            ctx.writeAndFlush(success).addListener(future -> {
                if (future.isSuccess()) {
                    handler.setState(ConnectionState.PLAY);
                    sendGameJoin(ctx);
                    sendPlaySetupPackets(ctx);
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
        if (decryptedNonce == null || sentNonce == null) return false;
        if (decryptedNonce.length != sentNonce.length) return false;
        for (int i = 0; i < sentNonce.length; i++) {
            if (decryptedNonce[i] != sentNonce[i]) return false;
        }
        return true;
    }

    private void sendGameJoin(ChannelHandlerContext ctx) {
        GameJoinS2C.CommonSpawnInfo spawnInfo = new GameJoinS2C.CommonSpawnInfo(
                new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld"),
                new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld"),
                0L,
                0,
                0,
                false,
                false,
                false,
                0,
                64
        );

        GameJoinS2C join = new GameJoinS2C(
                0,
                false,
                Set.of(new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld")),
                20,
                10,
                10,
                false,
                false,
                false,
                spawnInfo,
                false
        );

        ctx.writeAndFlush(join);
    }

    private void sendPlaySetupPackets(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new SetCenterChunkS2C(0, 0));
        ctx.writeAndFlush(new GameEventS2C((byte) 13, 0.0f));
        ctx.writeAndFlush(new PlayerPositionAndLookS2C(0, 64, 0, 0, 0, (byte) 0, 1));
        ctx.writeAndFlush(new PlayerInfoUpdateS2C(
                (byte) (0x01 | 0x02 | 0x04 | 0x08),
                new UUID[]{pendingHello.profileId()}));
    }

    private void log(String message) {
        handler.getLogger().log(message);
    }

    public MinecraftServerHandler.Logger getLogger() {
        return handler.getLogger();
    }
}
