package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.Packet;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2S;
import com.zouhmi.zymc.network.protocol.play.KeepAliveC2S;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookC2S;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2S;
import com.zouhmi.zymc.network.protocol.status.StatusResponseS2C;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public final class MinecraftServerHandler extends SimpleChannelInboundHandler<Packet<?>> {

    private final ConnectionRegistry connectionRegistry;
    private final ServerCommandCenter commandCenter;
    private final Logger logger;
    private final KeepAliveManager keepAliveManager;
    private java.util.function.Consumer<HandshakeC2S> handshakeCallback;
    private java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler;
    private java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler;

    public MinecraftServerHandler(ConnectionRegistry connectionRegistry, ServerCommandCenter commandCenter, Logger logger) {
        this(connectionRegistry, commandCenter, logger, null, null, null);
    }

    public MinecraftServerHandler(ConnectionRegistry connectionRegistry, ServerCommandCenter commandCenter, Logger logger,
                                   java.util.function.Consumer<HandshakeC2S> handshakeCallback,
                                   java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler,
                                   java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler) {
        this.connectionRegistry = connectionRegistry;
        this.commandCenter = commandCenter;
        this.logger = logger;
        this.handshakeCallback = handshakeCallback;
        this.loginHelloHandler = loginHelloHandler;
        this.loginKeyHandler = loginKeyHandler;
        this.keepAliveManager = new KeepAliveManager();
    }

    public void setLoginHelloHandler(java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> handler) {
        this.loginHelloHandler = handler;
    }

    public void setLoginKeyHandler(java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> handler) {
        this.loginKeyHandler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.log("Client connected: %s".formatted(ctx.channel().remoteAddress()));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.log("Client disconnected: %s".formatted(ctx.channel().remoteAddress()));
        keepAliveManager.stop(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) throws Exception {
        ConnectionState state = connectionRegistry.currentState();
        switch (state) {
            case HANDSHAKING -> {
                if (packet instanceof HandshakeC2S handshake) {
                    handleHandshake(ctx, handshake);
                }
            }
            case STATUS -> {
                if (packet instanceof StatusRequestC2S) {
                    handleStatusRequest(ctx);
                }
            }
            case LOGIN -> {
                if (packet instanceof LoginHelloC2S hello) {
                    handleLoginHello(ctx, hello);
                } else if (packet instanceof LoginKeyC2S key) {
                    handleLoginKey(ctx, key);
                }
            }
            case PLAY -> {
                if (packet instanceof KeepAliveC2S keepAlive) {
                    keepAliveManager.handleResponse(ctx.channel(), keepAlive.id());
                } else if (packet instanceof PlayerPositionAndLookC2S pos) {
                    logger.log("Player position: x=%.2f y=%.2f z=%.2f".formatted(pos.x(), pos.y(), pos.z()));
                } else if (packet instanceof AcceptTeleportationC2S teleport) {
                    logger.log("Teleport confirmed: id=%d".formatted(teleport.teleportId()));
                } else {
                    logger.log("Unhandled play packet: %s".formatted(packet.getClass().getSimpleName()));
                }
            }
            default -> throw new IllegalStateException("Unknown connection state: " + state);
        }
    }

    protected void handleHandshake(ChannelHandlerContext ctx, HandshakeC2S handshake) {
        if (handshake.protocolVersion() != ZMCVersion.PROTOCOL_VERSION) {
            disconnect(ctx, "Outdated server");
            return;
        }

        switch (handshake.intendedState()) {
            case STATUS -> {
                logger.log("Handshake from %s:%d - intended: STATUS".formatted(
                        handshake.address(), handshake.port()));
                setState(ConnectionState.STATUS);
            }
            case LOGIN -> {
                logger.log("Handshake from %s:%d - intended: LOGIN".formatted(
                        handshake.address(), handshake.port()));
                setState(ConnectionState.LOGIN);
            }
        }

        if (handshakeCallback != null) {
            handshakeCallback.accept(handshake);
        }
    }

    private void handleStatusRequest(ChannelHandlerContext ctx) {
        logger.log("Status request from %s".formatted(ctx.channel().remoteAddress()));
        String json = "{\"version\":{\"name\":\"%s\",\"protocol\":%d},"
                + "\"players\":{\"max\":%d},\"description\":{\"text\":\"%s\"}}"
                .formatted(ZMCVersion.MC_TARGET, ZMCVersion.PROTOCOL_VERSION, 20, "ZyMC");
        ctx.writeAndFlush(new StatusResponseS2C(json));
    }

    private void handleLoginHello(ChannelHandlerContext ctx, LoginHelloC2S hello) {
        logger.log("Login hello from %s (profile %s)".formatted(hello.name(), hello.profileId()));
        if (loginHelloHandler != null) {
            loginHelloHandler.accept(ctx, hello);
        }
    }

    private void handleLoginKey(ChannelHandlerContext ctx, LoginKeyC2S key) {
        logger.log("Login key received");
        if (loginKeyHandler != null) {
            loginKeyHandler.accept(ctx, key);
        }
    }

    private void disconnect(ChannelHandlerContext ctx, String reason) {
        ctx.close().addListener(future -> {
            if (!future.isSuccess()) {
                logger.log("Failed to close channel: %s".formatted(future.cause()));
            }
        });
    }

    void setState(ConnectionState state) {
        connectionRegistry.setState(state);
    }

    public ConnectionState getState() {
        return connectionRegistry.currentState();
    }

    public KeepAliveManager getKeepAliveManager() {
        return keepAliveManager;
    }

    void onChannelConnected(Channel channel) {
    }

    public interface ServerCommandCenter {
        void disconnect(Channel channel, String reason);
    }

    public interface Logger {
        void log(String message);
    }

    public Logger getLogger() {
        return logger;
    }
}
