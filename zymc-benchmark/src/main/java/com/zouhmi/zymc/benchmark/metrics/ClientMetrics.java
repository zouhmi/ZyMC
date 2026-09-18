package com.zouhmi.zymc.benchmark.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public final class ClientMetrics {
    private final String name;
    private volatile long connectStartTime;
    private volatile long handshakeSendTime;
    private volatile long loginHelloRecvTime;
    private volatile long loginKeySendTime;
    private volatile long loginSuccessRecvTime;
    private volatile long configurationStartTime;
    private volatile long configurationFinishTime;
    private volatile long playStartTime;
    private volatile long firstChunkRecvTime;
    private volatile boolean connected;
    private volatile boolean failed;
    private volatile String failureReason;
    private final LongAdder packetsReceived = new LongAdder();
    private final LongAdder packetsSent = new LongAdder();
    private final LongAdder bytesReceived = new LongAdder();
    private final LongAdder bytesSent = new LongAdder();
    private final LongAdder chunksReceived = new LongAdder();
    private final AtomicInteger keepAliveRoundTrips = new AtomicInteger();
    private final LongAdder keepAliveTotalNanos = new LongAdder();

    public ClientMetrics(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
    public boolean isFailed() { return failed; }
    public void setFailed(boolean failed) { this.failed = failed; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String reason) { this.failureReason = reason; }

    public void setConnectStartTime(long t) { this.connectStartTime = t; }
    public void setHandshakeSendTime(long t) { this.handshakeSendTime = t; }
    public void setLoginHelloRecvTime(long t) { this.loginHelloRecvTime = t; }
    public void setLoginKeySendTime(long t) { this.loginKeySendTime = t; }
    public void setLoginSuccessRecvTime(long t) { this.loginSuccessRecvTime = t; }
    public void setConfigurationStartTime(long t) { this.configurationStartTime = t; }
    public void setConfigurationFinishTime(long t) { this.configurationFinishTime = t; }
    public void setPlayStartTime(long t) { this.playStartTime = t; }
    public void setFirstChunkRecvTime(long t) { this.firstChunkRecvTime = t; }

    public long getConnectTimeMs() { return handshakeSendTime - connectStartTime; }
    public long getLoginTimeMs() { return loginSuccessRecvTime - loginKeySendTime; }
    public long getConfigurationTimeMs() { return configurationFinishTime - configurationStartTime; }
    public long getFullJoinTimeMs() { return playStartTime - connectStartTime; }
    public long getFirstChunkTimeMs() { return firstChunkRecvTime - connectStartTime; }

    public void recordPacketReceived(long bytes) { packetsReceived.increment(); bytesReceived.add(bytes); }
    public void recordPacketSent(long bytes) { packetsSent.increment(); bytesSent.add(bytes); }
    public void recordChunkReceived() { chunksReceived.increment(); }
    public void recordKeepAlive(long nanos) { keepAliveRoundTrips.incrementAndGet(); keepAliveTotalNanos.add(nanos); }

    public long getPacketsReceived() { return packetsReceived.sum(); }
    public long getPacketsSent() { return packetsSent.sum(); }
    public long getBytesReceived() { return bytesReceived.sum(); }
    public long getBytesSent() { return bytesSent.sum(); }
    public long getChunksReceived() { return chunksReceived.sum(); }
    public int getKeepAliveRoundTrips() { return keepAliveRoundTrips.get(); }
    public double getAvgKeepAliveMs() {
        int count = keepAliveRoundTrips.get();
        return count == 0 ? 0 : (keepAliveTotalNanos.sum() / 1_000_000.0) / count;
    }
}
