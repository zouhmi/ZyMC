package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.NettyPacketDecoder;
import com.zouhmi.zymc.network.protocol.NettyPacketEncoder;
import com.zouhmi.zymc.network.protocol.PacketRegistry;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2CCodec;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2CCodec;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2CCodec;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2S;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2SCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class MinecraftServerChannelInitializer extends ChannelInitializer<Channel> {

    private final PacketRegistry registry;
    private final MinecraftServerHandler handler;

    public MinecraftServerChannelInitializer(PacketRegistry registry, MinecraftServerHandler handler) {
        this.registry = registry;
        this.handler = handler;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new NettyPacketDecoder(registry));
        pipeline.addLast(handler);
        pipeline.addLast(new NettyPacketEncoder(registry));

        handler.onChannelConnected(ch);
    }

    public void registerHandshake() {
        registry.register(0x00, HandshakeC2S.class, new HandshakeC2SCodec());
    }

    public void registerStatus() {
        registry.register(0x00, StatusRequestC2S.class, new StatusRequestC2SCodec());
    }

    public void registerLogin() {
        // C2S packets (decoded by server)
        registry.register(0x00, LoginHelloC2S.class, new LoginHelloC2SCodec());
        registry.register(0x01, LoginKeyC2S.class, new LoginKeyC2SCodec());
        // S2C packet classes (encoded by server)
        registry.addEncoder(LoginHelloS2C.class, new LoginHelloS2CCodec(), 0x00);
        registry.addEncoder(LoginSuccessS2C.class, new LoginSuccessS2CCodec(), 0x02);
    }

    public void registerPlay() {
        // S2C play packets
        registry.addEncoder(GameJoinS2C.class, new GameJoinS2CCodec(), 0x00);
    }

    public void setState(ConnectionState state) {
        handler.setState(state);
    }
}
