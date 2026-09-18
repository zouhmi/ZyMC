package com.zouhmi.zymc.server;

import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.server.LoginManager;
import com.zouhmi.zymc.network.server.MinecraftServerChannelInitializer;
import com.zouhmi.zymc.network.server.MinecraftServerHandler;
import com.zouhmi.zymc.world.FlatWorldGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class ZyMCServer {

    public static void main(String[] args) throws InterruptedException {
        final boolean[] onlineMode = {true};
        final int[] port = {25565};

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--offline" -> onlineMode[0] = false;
                case "--port" -> port[0] = Integer.parseInt(args[++i]);
            }
        }

        System.out.println("ZyMC 0.1.0-SNAPSHOT");
        System.out.println("Starting Minecraft server for 1.21.11...");
        System.out.println("Online mode: " + onlineMode[0]);

        ConnectionRegistry connectionRegistry = new ConnectionRegistry();
        MinecraftServerHandler.Logger logger = System.out::println;

        FlatWorldGenerator worldGen = new FlatWorldGenerator();

        MinecraftServerChannelInitializer initializer =
                new MinecraftServerChannelInitializer(connectionRegistry, null, logger,
                        reg -> {
                            LoginManager lm = new LoginManager(reg, null,
                                    (x, z) -> worldGen.getChunk(x, z).toNetworkBytes(), onlineMode[0]);
                            lm.setLogger(logger);
                            return lm;
                        },
                        null, null, null);

        initializer.registerHandshake();
        initializer.registerStatus();
        initializer.registerLogin();
        initializer.registerConfiguration();
        initializer.registerPlay();

        int bossThreads = 1;
        int workerThreads = Runtime.getRuntime().availableProcessors();
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreads);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer);

        bootstrap.bind("0.0.0.0", port[0]).sync();
        System.out.println("Listening on 0.0.0.0:" + port[0]);

        Thread.currentThread().join();
    }
}
