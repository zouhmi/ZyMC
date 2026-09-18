package com.zouhmi.zymc.network.server;

import com.zouhmi.zymc.network.protocol.play.KeepAliveS2C;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class KeepAliveManager {

    private final ScheduledExecutorService scheduler;
    private final Map<Channel, Long> pendingKeepAlives;
    private final Map<Channel, ScheduledFuture<?>> scheduledTasks;
    private final AtomicBoolean running;

    public KeepAliveManager() {
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        this.pendingKeepAlives = new ConcurrentHashMap<>();
        this.scheduledTasks = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(true);
    }

    public void start(Channel channel) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (!running.get()) return;
            long id = ThreadLocalRandom.current().nextLong();
            pendingKeepAlives.put(channel, id);
            channel.writeAndFlush(new KeepAliveS2C(id));
        }, 0, 15000, TimeUnit.MILLISECONDS);
        scheduledTasks.put(channel, future);
    }

    public void handleResponse(Channel channel, long id) {
        Long expected = pendingKeepAlives.get(channel);
        if (expected != null && expected == id) {
            pendingKeepAlives.remove(channel);
        } else {
            channel.close();
        }
    }

    public void stop(Channel channel) {
        ScheduledFuture<?> future = scheduledTasks.remove(channel);
        if (future != null) {
            future.cancel(false);
        }
        pendingKeepAlives.remove(channel);
    }

    public void shutdown() {
        running.set(false);
        for (ScheduledFuture<?> future : scheduledTasks.values()) {
            future.cancel(false);
        }
        scheduledTasks.clear();
        pendingKeepAlives.clear();
        scheduler.shutdownNow();
    }
}
