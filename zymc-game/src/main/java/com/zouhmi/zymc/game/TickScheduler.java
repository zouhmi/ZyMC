package com.zouhmi.zymc.game;

public class TickScheduler {
    private volatile long currentTick;
    private volatile boolean running;
    private Thread tickThread;

    public TickScheduler() {
    }

    public void start(Runnable tickCallback) {
        running = true;
        tickThread = new Thread(() -> {
            while (running) {
                currentTick++;
                tickCallback.run();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "TickScheduler");
        tickThread.setDaemon(true);
        tickThread.start();
    }

    public void stop() {
        running = false;
        if (tickThread != null) {
            tickThread.interrupt();
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }
}
