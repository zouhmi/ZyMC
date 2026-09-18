package com.zouhmi.zymc.server;

import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.server.LoginManager;
import com.zouhmi.zymc.network.server.MinecraftServerChannelInitializer;
import com.zouhmi.zymc.network.server.MinecraftServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class ZyMCServer {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("ZyMC 0.1.0-SNAPSHOT");
        System.out.println("Starting Minecraft server for 1.21.11...");

        ConnectionRegistry connectionRegistry = new ConnectionRegistry();

        MinecraftServerHandler handler = new MinecraftServerHandler(
                connectionRegistry,
                null,
                System.out::println,
                null,
                null,
                null);

        LoginManager loginManager = new LoginManager(connectionRegistry, handler);

        handler.setLoginHelloHandler(loginManager::onLoginHello);
        handler.setLoginKeyHandler(loginManager::onLoginKey);

        MinecraftServerChannelInitializer initializer =
                new MinecraftServerChannelInitializer(connectionRegistry, handler);
        initializer.registerHandshake();
        initializer.registerStatus();
        initializer.registerLogin();
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

        bootstrap.bind("0.0.0.0", 25565).sync();
        System.out.println("Listening on 0.0.0.0:25565");

        Thread.currentThread().join();
    }
}
