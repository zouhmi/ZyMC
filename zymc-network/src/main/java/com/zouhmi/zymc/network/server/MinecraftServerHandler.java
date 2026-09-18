package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.Packet;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2S;
import com.zouhmi.zymc.network.protocol.status.StatusResponseS2C;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public final class MinecraftServerHandler extends SimpleChannelInboundHandler<Packet<?>> {

    private ConnectionState state = ConnectionState.HANDSHAKING;
    private final ServerCommandCenter commandCenter;
    private final Logger logger;
    private final java.util.function.Consumer<HandshakeC2S> handshakeCallback;
    private final java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler;
    private final java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler;

    public MinecraftServerHandler(ServerCommandCenter commandCenter, Logger logger) {
        this(commandCenter, logger, null, null, null);
    }

    public MinecraftServerHandler(ServerCommandCenter commandCenter, Logger logger,
                                   java.util.function.Consumer<HandshakeC2S> handshakeCallback,
                                   java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler,
                                   java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler) {
        this.commandCenter = commandCenter;
        this.logger = logger;
        this.handshakeCallback = handshakeCallback;
        this.loginHelloHandler = loginHelloHandler;
        this.loginKeyHandler = loginKeyHandler;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) throws Exception {
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
                // Play state packets from client are handled by the play state handler (not yet implemented)
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
                logger.log("Handshake from %s:%d — intended: STATUS".formatted(
                        handshake.address(), handshake.port()));
                setState(ConnectionState.STATUS);
            }
            case LOGIN -> {
                logger.log("Handshake from %s:%d — intended: LOGIN".formatted(
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
        // The login hello handler is responsible for generating the RSA keypair and sending LoginHelloS2C.
        // This is done by the loginHelloHandler callback (set by the server).
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
        this.state = state;
    }

    public ConnectionState getState() {
        return state;
    }

    void onChannelConnected(Channel channel) {
        // future: track connection, assign connection id, etc.
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
