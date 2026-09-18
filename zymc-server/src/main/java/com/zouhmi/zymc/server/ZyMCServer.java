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
        boolean onlineMode = true;
        int port = 25565;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--offline" -> onlineMode = false;
                case "--port" -> port = Integer.parseInt(args[++i]);
            }
        }

        System.out.println("ZyMC 0.1.0-SNAPSHOT");
        System.out.println("Starting Minecraft server for 1.21.11...");
        System.out.println("Online mode: " + onlineMode);

        ConnectionRegistry connectionRegistry = new ConnectionRegistry();
        FlatWorldGenerator worldGenerator = new FlatWorldGenerator();
        MinecraftServerHandler.Logger logger = System.out::println;

        LoginManager loginManager = new LoginManager(connectionRegistry, null,
                (x, z) -> worldGenerator.getChunk(x, z).toNetworkBytes(), onlineMode);
        loginManager.setLogger(logger);

        MinecraftServerChannelInitializer initializer =
                new MinecraftServerChannelInitializer(connectionRegistry, null, logger,
                        loginManager,
                        null,
                        (ctx, hello) -> loginManager.onLoginHello(ctx, hello),
                        (ctx, key) -> loginManager.onLoginKey(ctx, key));

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

        bootstrap.bind("0.0.0.0", port).sync();
        System.out.println("Listening on 0.0.0.0:" + port);

        Thread.currentThread().join();
    }
}
