package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.network.crypto.RSAEngine;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.KnownPacksS2C;
import com.zouhmi.zymc.network.protocol.configuration.PluginMessageConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.RegistryDataS2C;
import com.zouhmi.zymc.network.protocol.configuration.UpdateTagsS2C;
import com.zouhmi.zymc.network.protocol.play.ChunkDataAndUpdateLightS2C;
import com.zouhmi.zymc.network.protocol.play.GameEventS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerAbilitiesS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerInfoUpdateS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookS2C;
import com.zouhmi.zymc.network.protocol.play.SetCenterChunkS2C;
import com.zouhmi.zymc.network.protocol.play.SetSimulationDistanceS2C;
import com.zouhmi.zymc.network.protocol.play.SetTimeS2C;
import com.zouhmi.zymc.network.protocol.play.SystemChatS2C;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import io.netty.channel.ChannelHandlerContext;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public final class LoginManager {

    private final ConnectionRegistry connectionRegistry;
    private final RSAEngine rsaEngine;
    private MinecraftServerHandler handler;
    private final BiFunction<Integer, Integer, byte[]> chunkProvider;
    private final String serverId;
    private MinecraftServerHandler.Logger overrideLogger;

    private ChannelHandlerContext ctx;
    private LoginHelloC2S pendingHello;
    private byte[] sentNonce;
    private boolean configurationSent;

    public LoginManager(ConnectionRegistry connectionRegistry, MinecraftServerHandler handler,
                         BiFunction<Integer, Integer, byte[]> chunkProvider) {
        this.connectionRegistry = connectionRegistry;
        this.handler = handler;
        this.rsaEngine = new RSAEngine();
        this.chunkProvider = chunkProvider;
        this.serverId = "";
    }

    public void setHandler(MinecraftServerHandler handler) {
        this.handler = handler;
    }

    public void setLogger(MinecraftServerHandler.Logger logger) {
        this.overrideLogger = logger;
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
                    handler.setState(ConnectionState.CONFIGURATION);
                    sendConfiguration(ctx);
                } else {
                    ctx.close();
                }
            });
        } catch (Exception e) {
            log("Login key processing failed: " + e.getMessage());
            ctx.close();
        }
    }

    public void onConfigurationFinish(ChannelHandlerContext ctx) {
        if (configurationSent) return;
        configurationSent = true;

        handler.setState(ConnectionState.PLAY);
        sendGameJoin(ctx);
        sendPlaySetupPackets(ctx);
        sendChunks(ctx);
        handler.getKeepAliveManager().start(ctx.channel());
    }

    private boolean matchesNonce(byte[] decryptedNonce) {
        if (decryptedNonce == null || sentNonce == null) return false;
        if (decryptedNonce.length != sentNonce.length) return false;
        for (int i = 0; i < sentNonce.length; i++) {
            if (decryptedNonce[i] != sentNonce[i]) return false;
        }
        return true;
    }

    private void sendConfiguration(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new PluginMessageConfigurationS2C("minecraft:brand", new byte[]{0x06, 'Z', 'y', 'M', 'C', ' ', '1', '0'}));
        ctx.writeAndFlush(new KnownPacksS2C("minecraft", "minecraft", "1.21.4"));
        ctx.writeAndFlush(new UpdateTagsS2C());
        ctx.writeAndFlush(new FinishConfigurationS2C());
    }

    private void sendGameJoin(ChannelHandlerContext ctx) {
        GameJoinS2C.CommonSpawnInfo spawnInfo = new GameJoinS2C.CommonSpawnInfo(
                new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld"),
                new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld"),
                0L, 0, 0, false, false, false, 0, 64);

        GameJoinS2C join = new GameJoinS2C(
                1, false,
                Set.of(new GameJoinS2C.DimensionKey("minecraft", "dimension_type",
                        "minecraft", "overworld")),
                20, 10, 10, false, false, false, spawnInfo, false);

        ctx.writeAndFlush(join);
    }

    private void sendPlaySetupPackets(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new PlayerInfoUpdateS2C(
                (byte) (0x01 | 0x02 | 0x04 | 0x08),
                new UUID[]{pendingHello.profileId()}));
        ctx.writeAndFlush(new SetCenterChunkS2C(0, 0));
        ctx.writeAndFlush(new GameEventS2C((byte) 13, 0.0f));
        ctx.writeAndFlush(new PlayerAbilitiesS2C((byte) 0x01, 0.05f, 110.0f));
        ctx.writeAndFlush(new SetSimulationDistanceS2C(10));
        ctx.writeAndFlush(new SetTimeS2C(6000, 6000, false));
        ctx.writeAndFlush(new SystemChatS2C("{\"text\":\"Welcome to ZyMC!\",\"color\":\"green\"}", false));
        ctx.writeAndFlush(new PlayerPositionAndLookS2C(0.5, 65.0, 0.5, 0, 0, (byte) 0, 1));
    }

    private void sendChunks(ChannelHandlerContext ctx) {
        int viewDistance = 4;
        for (int x = -viewDistance; x <= viewDistance; x++) {
            for (int z = -viewDistance; z <= viewDistance; z++) {
                byte[] data = chunkProvider.apply(x, z);
                ctx.writeAndFlush(new ChunkDataAndUpdateLightS2C(x, z, data));
            }
        }
        log("Sent %d chunks".formatted((viewDistance * 2 + 1) * (viewDistance * 2 + 1)));
    }

    private void log(String message) {
        if (handler != null) {
            handler.getLogger().log(message);
        } else if (overrideLogger != null) {
            overrideLogger.log(message);
        }
    }

    public MinecraftServerHandler.Logger getLogger() {
        if (handler != null) return handler.getLogger();
        return overrideLogger;
    }
}
