package com.zouhmi.zymc.benchmark.client;

import com.zouhmi.zymc.benchmark.metrics.ClientMetrics;
import com.zouhmi.zymc.benchmark.metrics.MetricsCollector;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.protocol.ConnectionState;
import com.zouhmi.zymc.network.protocol.NettyPacketDecoder;
import com.zouhmi.zymc.network.protocol.NettyPacketEncoder;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2S;
import com.zouhmi.zymc.network.protocol.handshake.HandshakeC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2S;
import com.zouhmi.zymc.network.protocol.login.LoginHelloC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2C;
import com.zouhmi.zymc.network.protocol.login.LoginHelloS2CCodec;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2S;
import com.zouhmi.zymc.network.protocol.login.LoginKeyC2SCodec;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2C;
import com.zouhmi.zymc.network.protocol.login.LoginSuccessS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationC2S;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationC2SCodec;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.FinishConfigurationS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.KnownPacksS2C;
import com.zouhmi.zymc.network.protocol.configuration.KnownPacksS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.PingConfigurationS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.PluginMessageConfigurationS2C;
import com.zouhmi.zymc.network.protocol.configuration.PluginMessageConfigurationS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.RegistryDataS2C;
import com.zouhmi.zymc.network.protocol.configuration.RegistryDataS2CCodec;
import com.zouhmi.zymc.network.protocol.configuration.UpdateTagsS2C;
import com.zouhmi.zymc.network.protocol.configuration.UpdateTagsS2CCodec;
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2S;
import com.zouhmi.zymc.network.protocol.play.AcceptTeleportationC2SCodec;
import com.zouhmi.zymc.network.protocol.play.ChunkDataAndUpdateLightS2C;
import com.zouhmi.zymc.network.protocol.play.ChunkDataAndUpdateLightS2CCodec;
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
import com.zouhmi.zymc.network.protocol.play.PlayerAbilitiesS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerAbilitiesS2CCodec;
import com.zouhmi.zymc.network.protocol.play.PlayerInfoUpdateS2C;
import com.zouhmi.zymc.network.protocol.play.PlayerInfoUpdateS2CCodec;
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
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class BenchmarkClient {

    private final String host;
    private final int port;
    private final String playerName;
    private final UUID playerUuid;
    private final ClientMetrics metrics;
    private final MetricsCollector collector;
    private final CountDownLatch disconnectLatch;
    private Channel channel;

    public BenchmarkClient(String host, int port, int index, MetricsCollector collector) {
        this.host = host;
        this.port = port;
        this.playerName = "BenchBot_" + index;
        this.playerUuid = UUID.randomUUID();
        this.metrics = new ClientMetrics(playerName);
        this.collector = collector;
        this.disconnectLatch = new CountDownLatch(1);
        collector.addClient(metrics);
    }

    public ClientMetrics getMetrics() { return metrics; }

    public void connect() throws Exception {
        ConnectionRegistry clientRegistry = createClientRegistry();

        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<>() {
                 @Override
                 protected void initChannel(Channel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new LoggingHandler(LogLevel.WARN));
                     p.addLast("packet-decoder", new NettyPacketDecoder(clientRegistry));
                     p.addLast(new BenchmarkClientHandler(metrics, clientRegistry, playerUuid, playerName));
                     p.addLast("packet-encoder", new NettyPacketEncoder(clientRegistry));
                 }
             });

            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();

            disconnectLatch.await(30, TimeUnit.SECONDS);
            group.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
        } catch (Exception e) {
            if (!metrics.isFailed()) {
                metrics.setFailed(true);
                metrics.setFailureReason(e.getMessage());
            }
            group.shutdownNow();
        }
    }

    public void disconnect() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        disconnectLatch.countDown();
    }

    private ConnectionRegistry createClientRegistry() {
        ConnectionRegistry registry = new ConnectionRegistry();

        registry.register(ConnectionState.HANDSHAKING, 0x00, HandshakeC2S.class, new HandshakeC2SCodec());

        registry.register(ConnectionState.LOGIN, 0x00, LoginHelloS2C.class, new LoginHelloS2CCodec());
        registry.register(ConnectionState.LOGIN, 0x02, LoginSuccessS2C.class, new LoginSuccessS2CCodec());
        registry.addEncoder(ConnectionState.LOGIN, LoginHelloC2S.class, new LoginHelloC2SCodec(), 0x00);
        registry.addEncoder(ConnectionState.LOGIN, LoginKeyC2S.class, new LoginKeyC2SCodec(), 0x01);

        registry.register(ConnectionState.CONFIGURATION, 0x00, PluginMessageConfigurationS2C.class, new PluginMessageConfigurationS2CCodec());
        registry.register(ConnectionState.CONFIGURATION, 0x02, FinishConfigurationS2C.class, new FinishConfigurationS2CCodec());
        registry.register(ConnectionState.CONFIGURATION, 0x03, PingConfigurationS2C.class, new PingConfigurationS2CCodec());
        registry.register(ConnectionState.CONFIGURATION, 0x05, RegistryDataS2C.class, new RegistryDataS2CCodec());
        registry.register(ConnectionState.CONFIGURATION, 0x0D, UpdateTagsS2C.class, new UpdateTagsS2CCodec());
        registry.register(ConnectionState.CONFIGURATION, 0x0E, KnownPacksS2C.class, new KnownPacksS2CCodec());
        registry.addEncoder(ConnectionState.CONFIGURATION, FinishConfigurationC2S.class, new FinishConfigurationC2SCodec(), 0x02);

        registry.register(ConnectionState.PLAY, 0x20, DisconnectPlayS2C.class, new DisconnectPlayS2CCodec());
        registry.register(ConnectionState.PLAY, 0x26, GameEventS2C.class, new GameEventS2CCodec());
        registry.register(ConnectionState.PLAY, 0x2B, KeepAliveS2C.class, new KeepAliveS2CCodec());
        registry.register(ConnectionState.PLAY, 0x2C, ChunkDataAndUpdateLightS2C.class, new ChunkDataAndUpdateLightS2CCodec());
        registry.register(ConnectionState.PLAY, 0x30, GameJoinS2C.class, new GameJoinS2CCodec());
        registry.register(ConnectionState.PLAY, 0x3E, PlayerAbilitiesS2C.class, new PlayerAbilitiesS2CCodec());
        registry.register(ConnectionState.PLAY, 0x44, PlayerInfoUpdateS2C.class, new PlayerInfoUpdateS2CCodec());
        registry.register(ConnectionState.PLAY, 0x46, PlayerPositionAndLookS2C.class, new PlayerPositionAndLookS2CCodec());
        registry.register(ConnectionState.PLAY, 0x47, PlayerRotationS2C.class, new PlayerRotationS2CCodec());
        registry.register(ConnectionState.PLAY, 0x5C, SetCenterChunkS2C.class, new SetCenterChunkS2CCodec());
        registry.register(ConnectionState.PLAY, 0x5F, SpawnPositionS2C.class, new SpawnPositionS2CCodec());
        registry.register(ConnectionState.PLAY, 0x6D, SetSimulationDistanceS2C.class, new SetSimulationDistanceS2CCodec());
        registry.register(ConnectionState.PLAY, 0x6F, SetTimeS2C.class, new SetTimeS2CCodec());
        registry.register(ConnectionState.PLAY, 0x77, SystemChatS2C.class, new SystemChatS2CCodec());
        registry.addEncoder(ConnectionState.PLAY, AcceptTeleportationC2S.class, new AcceptTeleportationC2SCodec(), 0x00);
        registry.addEncoder(ConnectionState.PLAY, KeepAliveC2S.class, new KeepAliveC2SCodec(), 0x1B);

        return registry;
    }
}
