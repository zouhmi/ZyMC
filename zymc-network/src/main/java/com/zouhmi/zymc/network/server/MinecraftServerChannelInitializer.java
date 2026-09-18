package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.NettyPacketDecoder;
import com.zouhmi.zymc.network.protocol.NettyPacketEncoder;
import com.zouhmi.zymc.network.protocol.configuration.ClientInformationConfigurationC2S;
import com.zouhmi.zymc.network.protocol.configuration.ClientInformationConfigurationC2SCodec;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationC2S;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationC2SCodec;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.KnownPacksS2C;
import com.zouhmi.zymc.network.protocol.configuration.KnownPacksS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationC2S;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationC2SCodec;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.PluginMessageConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.PluginMessageConfigurationS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.RegistryDataS2C;
import com.zouhmi.zymc.network.protocol.configuration.RegistryDataS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.UpdateTagsS2C;
import com.zouhmi.zymc.network.protocol.configuration.UpdateTagsS2CCodec;
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
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2S;
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2SCodec;
import com.zouhmi.zymc.network.protocol.play.ChatCommandC2S;
import com.zouhmi.zymc.network.protocol.play.ChatCommandC2SCodec;
import com.zouhmi.zymc.network.protocol.play.ChatMessageC2S;
import com.zouhmi.zymc.network.protocol.play.ChatMessageC2SCodec;
import com.zouhmi.zymc.network.protocol.play.ChunkDataAndUpdateLightS2C;
import com.zouhmi.zymc.network.protocol.play.ChunkDataAndUpdateLightS2CCodec;
import com.zouhmi.zymc.network.protocol.play.ClientCommandC2S;
import com.zouhmi.zymc.network.protocol.play.ClientCommandC2SCodec;
import com.zouhmi.zymc.network.protocol.play.ClientInformationC2S;
import com.zouhmi.zymc.network.protocol.play.ClientInformationC2SCodec;
import com.zouhmi.zymc.network.protocol.play.DisconnectPlayS2C;
import com.zouhmi.zymc.network.protocol.play.DisconnectPlayS2CCodec;
import com.zouhmi.zymc.network.protocol.play.GameEventS2C;
import com.zouhmi.zymc.network.protocol.play.GameEventS2CCodec;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2C;
import com.zouhmi.zymc.network.protocol.play.GameJoinS2CCodec;
import com.zouhmi.zymc.network.protocol.play.KeepAliveC2S;
import com.zouhmi.zymc.network.protocol.play.KeepAliveC2SCodec;
import com.zouhmi.zymc.network.protocol.play.KeepAliveS2C;
import com.zouhmi.zymc.network.protocol.play.KeepAliveS2CCodec;
import com.zouhmi.zymc.network.protocol.play.MovePlayerPosC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerPosC2SCodec;
import com.zouhmi.zymc.network.protocol.play.MovePlayerPosRotC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerPosRotC2SCodec;
import com.zouhmi.zymc.network.protocol.play.MovePlayerRotC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerRotC2SCodec;
import com.zouhmi.zymc.network.protocol.play.MovePlayerStatusOnlyC2S;
import com.zouhmi.zymc.network.protocol.play.MovePlayerStatusOnlyC2SCodec;
import com.zouhmi.zymc.network.protocol.play.PlayerAbilitiesS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerAbilitiesS2CCodec;
import com.zouhmi.zymc.network.protocol.play.PlayerInfoUpdateS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerInfoUpdateS2CCodec;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookC2S;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookC2SCodec;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerPositionAndLookS2CCodec;
import com.zouhmi.zymc.network.protocol.play.PlayerRotationS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerRotationS2CCodec;
import com.zouhmi.zymc.network.protocol.play.SetCenterChunkS2C;
import com.zouhmi.zymc.network.protocol.play.SetCenterChunkS2CCodec;
import com.zouhmi.zymc.network.protocol.play.SetSimulationDistanceS2C;
import com.zouhmi.zymc.network.protocol.play.SetSimulationDistanceS2CCodec;
import com.zouhmi.zymc.network.protocol.play.SetTimeS2C;
import com.zouhmi.zymc.network.protocol.play.SetTimeS2CCodec;
import com.zouhmi.zymc.network.protocol.play.SpawnPositionS2C;
import com.zouhmi.zymc.network.protocol.play.SpawnPositionS2CCodec;
import com.zouhmi.zymc.network.protocol.play.SynchronizePlayerPositionS2C;
import com.zouhmi.zymc.network.protocol.play.SynchronizePlayerPositionS2CCodec;
import com.zouhmi.zymc.network.protocol.play.SystemChatS2C;
import com.zouhmi.zymc.network.protocol.play.SystemChatS2CCodec;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2S;
import com.zouhmi.zymc.network.protocol.status.StatusRequestC2SCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MinecraftServerChannelInitializer extends ChannelInitializer<Channel> {

    private final ConnectionRegistry connectionRegistry;
    private final MinecraftServerHandler.ServerCommandCenter commandCenter;
    private final MinecraftServerHandler.Logger logger;
    private final LoginManager loginManager;
    private final Consumer<HandshakeC2S> handshakeCallback;
    private final BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler;
    private final BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler;

    public MinecraftServerChannelInitializer(ConnectionRegistry connectionRegistry,
                                              MinecraftServerHandler.ServerCommandCenter commandCenter,
                                              MinecraftServerHandler.Logger logger,
                                              LoginManager loginManager,
                                              Consumer<HandshakeC2S> handshakeCallback,
                                              BiConsumer<ChannelHandlerContext, LoginHelloC2S> loginHelloHandler,
                                              BiConsumer<ChannelHandlerContext, LoginKeyC2S> loginKeyHandler) {
        this.connectionRegistry = connectionRegistry;
        this.commandCenter = commandCenter;
        this.logger = logger;
        this.loginManager = loginManager;
        this.handshakeCallback = handshakeCallback;
        this.loginHelloHandler = loginHelloHandler;
        this.loginKeyHandler = loginKeyHandler;
    }

    public MinecraftServerChannelInitializer(ConnectionRegistry connectionRegistry, MinecraftServerHandler handler) {
        this(connectionRegistry, null, handler.getLogger(), null, null, null, null);
    }

    @Override
    protected void initChannel(Channel ch) {
        MinecraftServerHandler handler = new MinecraftServerHandler(
                connectionRegistry, commandCenter, logger,
                loginManager, handshakeCallback, loginHelloHandler, loginKeyHandler);

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new NettyPacketDecoder(connectionRegistry));
        pipeline.addLast(handler);
        pipeline.addLast(new NettyPacketEncoder(connectionRegistry));

        handler.onChannelConnected(ch);
    }

    public void registerHandshake() {
        connectionRegistry.register(ConnectionState.HANDSHAKING, 0x00, HandshakeC2S.class, new HandshakeC2SCodec());
    }

    public void registerStatus() {
        connectionRegistry.register(ConnectionState.STATUS, 0x00, StatusRequestC2S.class, new StatusRequestC2SCodec());
    }

    public void registerLogin() {
        connectionRegistry.register(ConnectionState.LOGIN, 0x00, LoginHelloC2S.class, new LoginHelloC2SCodec());
        connectionRegistry.register(ConnectionState.LOGIN, 0x01, LoginKeyC2S.class, new LoginKeyC2SCodec());
        connectionRegistry.addEncoder(ConnectionState.LOGIN, LoginHelloS2C.class, new LoginHelloS2CCodec(), 0x00);
        connectionRegistry.addEncoder(ConnectionState.LOGIN, LoginSuccessS2C.class, new LoginSuccessS2CCodec(), 0x02);
    }

    public void registerConfiguration() {
        connectionRegistry.register(ConnectionState.CONFIGURATION, 0x00, ClientInformationConfigurationC2S.class, new ClientInformationConfigurationC2SCodec());
        connectionRegistry.register(ConnectionState.CONFIGURATION, 0x02, FinishConfigurationC2S.class, new FinishConfigurationC2SCodec());
        connectionRegistry.register(ConnectionState.CONFIGURATION, 0x03, PingConfigurationC2S.class, new PingConfigurationC2SCodec());
        connectionRegistry.addEncoder(ConnectionState.CONFIGURATION, RegistryDataS2C.class, new RegistryDataS2CCodec(), 0x05);
        connectionRegistry.addEncoder(ConnectionState.CONFIGURATION, FinishConfigurationS2C.class, new FinishConfigurationS2CCodec(), 0x02);
        connectionRegistry.addEncoder(ConnectionState.CONFIGURATION, KnownPacksS2C.class, new KnownPacksS2CCodec(), 0x0E);
        connectionRegistry.addEncoder(ConnectionState.CONFIGURATION, UpdateTagsS2C.class, new UpdateTagsS2CCodec(), 0x0D);
        connectionRegistry.addEncoder(ConnectionState.CONFIGURATION, PluginMessageConfigurationS2C.class, new PluginMessageConfigurationS2CCodec(), 0x00);
        connectionRegistry.addEncoder(ConnectionState.CONFIGURATION, PingConfigurationS2C.class, new PingConfigurationS2CCodec(), 0x03);
    }

    public void registerPlay() {
        // S2C packets
        connectionRegistry.addEncoder(ConnectionState.PLAY, GameJoinS2C.class, new GameJoinS2CCodec(), 0x30);
        connectionRegistry.addEncoder(ConnectionState.PLAY, KeepAliveS2C.class, new KeepAliveS2CCodec(), 0x2B);
        connectionRegistry.addEncoder(ConnectionState.PLAY, ChunkDataAndUpdateLightS2C.class, new ChunkDataAndUpdateLightS2CCodec(), 0x2C);
        connectionRegistry.addEncoder(ConnectionState.PLAY, PlayerPositionAndLookS2C.class, new PlayerPositionAndLookS2CCodec(), 0x46);
        connectionRegistry.addEncoder(ConnectionState.PLAY, SpawnPositionS2C.class, new SpawnPositionS2CCodec(), 0x5F);
        connectionRegistry.addEncoder(ConnectionState.PLAY, GameEventS2C.class, new GameEventS2CCodec(), 0x26);
        connectionRegistry.addEncoder(ConnectionState.PLAY, SetCenterChunkS2C.class, new SetCenterChunkS2CCodec(), 0x5C);
        connectionRegistry.addEncoder(ConnectionState.PLAY, PlayerInfoUpdateS2C.class, new PlayerInfoUpdateS2CCodec(), 0x44);
        connectionRegistry.addEncoder(ConnectionState.PLAY, SynchronizePlayerPositionS2C.class, new SynchronizePlayerPositionS2CCodec(), 0x46);
        connectionRegistry.addEncoder(ConnectionState.PLAY, PlayerAbilitiesS2C.class, new PlayerAbilitiesS2CCodec(), 0x3E);
        connectionRegistry.addEncoder(ConnectionState.PLAY, SystemChatS2C.class, new SystemChatS2CCodec(), 0x77);
        connectionRegistry.addEncoder(ConnectionState.PLAY, DisconnectPlayS2C.class, new DisconnectPlayS2CCodec(), 0x20);
        connectionRegistry.addEncoder(ConnectionState.PLAY, SetSimulationDistanceS2C.class, new SetSimulationDistanceS2CCodec(), 0x6D);
        connectionRegistry.addEncoder(ConnectionState.PLAY, SetTimeS2C.class, new SetTimeS2CCodec(), 0x6F);
        connectionRegistry.addEncoder(ConnectionState.PLAY, PlayerRotationS2C.class, new PlayerRotationS2CCodec(), 0x47);
        // C2S packets
        connectionRegistry.register(ConnectionState.PLAY, 0x1B, KeepAliveC2S.class, new KeepAliveC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x0, AcceptTeleportationC2S.class, new AcceptTeleportationC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x1E, MovePlayerPosRotC2S.class, new MovePlayerPosRotC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x1D, MovePlayerPosC2S.class, new MovePlayerPosC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x1F, MovePlayerRotC2S.class, new MovePlayerRotC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x20, MovePlayerStatusOnlyC2S.class, new MovePlayerStatusOnlyC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x6, ChatCommandC2S.class, new ChatCommandC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0x8, ChatMessageC2S.class, new ChatMessageC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0xB, ClientCommandC2S.class, new ClientCommandC2SCodec());
        connectionRegistry.register(ConnectionState.PLAY, 0xD, ClientInformationC2S.class, new ClientInformationC2SCodec());
    }
}
