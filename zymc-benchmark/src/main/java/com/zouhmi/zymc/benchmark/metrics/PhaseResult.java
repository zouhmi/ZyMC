package com.zouhmi.zymc.benchmark.metrics;

import java.util.List;

public final class PhaseResult {
    private final int heapMB;
    private final long durationMs;
    private final int totalClients;
    private final int successfulConnections;
    private final int failedConnections;
    private final double avgConnectTimeMs;
    private final double avgLoginTimeMs;
    private final double avgConfigurationTimeMs;
    private final double avgFullJoinTimeMs;
    private final double avgFirstChunkTimeMs;
    private final long totalPacketsReceived;
    private final long totalPacketsSent;
    private final long totalBytesReceived;
    private final long totalBytesSent;
    private final long totalChunksReceived;
    private final double avgKeepAliveMs;
    private final double maxKeepAliveMs;
    private final double p95KeepAliveMs;
    private final long[] gcPauseSamples;
    private final long totalGcPauseNanos;
    private final int gcCount;
    private final long[] heapUsageSamples;
    private final long peakHeapUsed;
    private final long avgHeapUsed;

    public PhaseResult(int heapMB, long durationMs, int totalClients, int successful, int failed,
                       double avgConnect, double avgLogin, double avgConfig, double avgFullJoin, double avgFirstChunk,
                       long totalPktsRecv, long totalPktsSent, long totalBytesRecv, long totalBytesSent,
                       long totalChunks, double avgKA, double maxKA, double p95KA,
                       long[] gcPauses, long totalGcPause, int gcCount,
                       long[] heapSamples, long peakHeap, long avgHeap) {
        this.heapMB = heapMB;
        this.durationMs = durationMs;
        this.totalClients = totalClients;
        this.successfulConnections = successful;
        this.failedConnections = failed;
        this.avgConnectTimeMs = avgConnect;
        this.avgLoginTimeMs = avgLogin;
        this.avgConfigurationTimeMs = avgConfig;
        this.avgFullJoinTimeMs = avgFullJoin;
        this.avgFirstChunkTimeMs = avgFirstChunk;
        this.totalPacketsReceived = totalPktsRecv;
        this.totalPacketsSent = totalPktsSent;
        this.totalBytesReceived = totalBytesRecv;
        this.totalBytesSent = totalBytesSent;
        this.totalChunksReceived = totalChunks;
        this.avgKeepAliveMs = avgKA;
        this.maxKeepAliveMs = maxKA;
        this.p95KeepAliveMs = p95KA;
        this.gcPauseSamples = gcPauses;
        this.totalGcPauseNanos = totalGcPause;
        this.gcCount = gcCount;
        this.heapUsageSamples = heapSamples;
        this.peakHeapUsed = peakHeap;
        this.avgHeapUsed = avgHeap;
    }

    public int getHeapMB() { return heapMB; }
    public long getDurationMs() { return durationMs; }
    public int getTotalClients() { return totalClients; }
    public int getSuccessfulConnections() { return successfulConnections; }
    public int getFailedConnections() { return failedConnections; }
    public double getAvgConnectTimeMs() { return avgConnectTimeMs; }
    public double getAvgLoginTimeMs() { return avgLoginTimeMs; }
    public double getAvgConfigurationTimeMs() { return avgConfigurationTimeMs; }
    public double getAvgFullJoinTimeMs() { return avgFullJoinTimeMs; }
    public double getAvgFirstChunkTimeMs() { return avgFirstChunkTimeMs; }
    public long getTotalPacketsReceived() { return totalPacketsReceived; }
    public long getTotalPacketsSent() { return totalPacketsSent; }
    public long getTotalBytesReceived() { return totalBytesReceived; }
    public long getTotalBytesSent() { return totalBytesSent; }
    public long getTotalChunksReceived() { return totalChunksReceived; }
    public double getAvgKeepAliveMs() { return avgKeepAliveMs; }
    public double getMaxKeepAliveMs() { return maxKeepAliveMs; }
    public double getP95KeepAliveMs() { return p95KeepAliveMs; }
    public long[] getGcPauseSamples() { return gcPauseSamples; }
    public long getTotalGcPauseNanos() { return totalGcPauseNanos; }
    public int getGcCount() { return gcCount; }
    public long[] getHeapUsageSamples() { return heapUsageSamples; }
    public long getPeakHeapUsed() { return peakHeapUsed; }
    public long getAvgHeapUsed() { return avgHeapUsed; }
    public double getGcPausePercent() { return durationMs == 0 ? 0 : (totalGcPauseNanos / 1_000_000.0) / durationMs * 100.0; }
    public double getThroughputPps() { return durationMs == 0 ? 0 : (totalPacketsReceived + totalPacketsSent) * 1000.0 / durationMs; }
    public double getThroughputBps() { return durationMs == 0 ? 0 : (totalBytesReceived + totalBytesSent) * 1000.0 / durationMs; }
}
