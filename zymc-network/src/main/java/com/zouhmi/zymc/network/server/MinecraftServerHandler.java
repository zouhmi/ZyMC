package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.Packet;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationC2S;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationC2S;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2S;
import com.zouhmi.zymc.network.protocol.play.ChatCommandC2S;
import com.zouhmi.zymc.network.protocol.play.ChatMessageC2S;
import com.zouhmi.zymc.network.protocol.play.ClientCommandC2S;
import com.zouhmi.zymc.network.protocol.play.ClientInformationC2S;
import com.zouhmi.zymc.network.protocol.play.KeepAliveC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerPosC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerPosRotC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerRotC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerStatusOnlyC2S;
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
    private final LoginManager loginManager;
    private java.util.function.Consumer<HandshakeC2S> handshakeCallback;
    private java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler;
    private java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler;

    public MinecraftServerHandler(ConnectionRegistry connectionRegistry, ServerCommandCenter commandCenter, Logger logger) {
        this(connectionRegistry, commandCenter, logger, null, null, null, null);
    }

    public MinecraftServerHandler(ConnectionRegistry connectionRegistry, ServerCommandCenter commandCenter, Logger logger,
                                   LoginManager loginManager,
                                   java.util.function.Consumer<HandshakeC2S> handshakeCallback,
                                   java.util.function.BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler,
                                   java.util.function.BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler) {
        this.connectionRegistry = connectionRegistry;
        this.commandCenter = commandCenter;
        this.logger = logger;
        this.loginManager = loginManager;
        this.handshakeCallback = handshakeCallback;
        this.loginHelloHandler = loginHelloHandler;
        this.loginKeyHandler = loginKeyHandler;
        this.keepAliveManager = new KeepAliveManager();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(ConnectionRegistry.STATE_KEY).set(ConnectionState.HANDSHAKING);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) throws Exception {
        ConnectionState state = getState(ctx);
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
            case CONFIGURATION -> handleConfigurationPacket(ctx, packet);
            case PLAY -> handlePlayPacket(ctx, packet);
            default -> throw new IllegalStateException("Unknown connection state: " + state);
        }
    }

    private void handleConfigurationPacket(ChannelHandlerContext ctx, Packet<?> packet) {
        if (packet instanceof FinishConfigurationC2S) {
            if (loginManager != null) {
                loginManager.onConfigurationFinish(ctx);
            }
        }
    }

    private void handlePlayPacket(ChannelHandlerContext ctx, Packet<?> packet) {
        if (packet instanceof KeepAliveC2S keepAlive) {
            keepAliveManager.handleResponse(ctx.channel(), keepAlive.id());
        } else if (packet instanceof AcceptTeleportationC2S) {
        } else if (packet instanceof MovePlayerPosC2S) {
        } else if (packet instanceof MovePlayerPosRotC2S) {
        } else if (packet instanceof MovePlayerRotC2S) {
        } else if (packet instanceof MovePlayerStatusOnlyC2S) {
        } else if (packet instanceof ChatCommandC2S) {
        } else if (packet instanceof ChatMessageC2S) {
        } else if (packet instanceof ClientCommandC2S) {
        } else if (packet instanceof ClientInformationC2S) {
        }
    }

    protected void handleHandshake(ChannelHandlerContext ctx, HandshakeC2S handshake) {
        if (handshake.protocolVersion() != ZMCVersion.PROTOCOL_VERSION) {
            ctx.close();
            return;
        }

        switch (handshake.intendedState()) {
            case STATUS -> setState(ctx, ConnectionState.STATUS);
            case LOGIN -> setState(ctx, ConnectionState.LOGIN);
        }

        if (handshakeCallback != null) {
            handshakeCallback.accept(handshake);
        }
    }

    private void handleStatusRequest(ChannelHandlerContext ctx) {
        String json = "{\"version\":{\"name\":\"%s\",\"protocol\":%d},"
                + "\"players\":{\"max\":%d,\"online\":0},\"description\":{\"text\":\"%s\"}}"
                .formatted(ZMCVersion.MC_TARGET, ZMCVersion.PROTOCOL_VERSION, 20, "ZyMC");
        ctx.writeAndFlush(new StatusResponseS2C(json));
    }

    private void handleLoginHello(ChannelHandlerContext ctx, LoginHelloC2S hello) {
        if (loginManager != null) {
            loginManager.onLoginHello(ctx, hello);
        } else if (loginHelloHandler != null) {
            loginHelloHandler.accept(ctx, hello);
        }
    }

    private void handleLoginKey(ChannelHandlerContext ctx, LoginKeyC2S key) {
        if (loginManager != null) {
            loginManager.onLoginKey(ctx, key);
        } else if (loginKeyHandler != null) {
            loginKeyHandler.accept(ctx, key);
        }
    }

    void setState(ChannelHandlerContext ctx, ConnectionState state) {
        ctx.channel().attr(ConnectionRegistry.STATE_KEY).set(state);
    }

    public ConnectionState getState(ChannelHandlerContext ctx) {
        ConnectionState s = ctx.channel().attr(ConnectionRegistry.STATE_KEY).get();
        return s != null ? s : ConnectionState.HANDSHAKING;
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
