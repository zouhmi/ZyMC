package com.zouhmi.zymc.benchmark.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public final class MetricsCollector {
    private final CopyOnWriteArrayList<ClientMetrics> clientMetrics = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<long[]> gcPauseSamples = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Long> heapSamples = new CopyOnWriteArrayList<>();
    private volatile long startTimeMs;
    private volatile long endTimeMs;

    public void start() {
        startTimeMs = System.currentTimeMillis();
    }

    public void stop() {
        endTimeMs = System.currentTimeMillis();
    }

    public void addClient(ClientMetrics metrics) {
        clientMetrics.add(metrics);
    }

    public void recordGcPause(long pauseNanos) {
        gcPauseSamples.add(new long[]{System.currentTimeMillis() - startTimeMs, pauseNanos});
    }

    public void recordHeapUsage(long usedBytes) {
        heapSamples.add(usedBytes);
    }

    public void sampleJVM(long pid) {
        Runtime rt = Runtime.getRuntime();
        heapSamples.add(rt.totalMemory() - rt.freeMemory());
    }

    public PhaseResult buildResult(int heapMB) {
        long durationMs = endTimeMs - startTimeMs;
        int total = clientMetrics.size();
        int success = 0;
        int failed = 0;
        List<Double> connectTimes = new ArrayList<>();
        List<Double> loginTimes = new ArrayList<>();
        List<Double> configTimes = new ArrayList<>();
        List<Double> fullJoinTimes = new ArrayList<>();
        List<Double> firstChunkTimes = new ArrayList<>();
        List<Double> keepAliveTimes = new ArrayList<>();
        long totalPktsRecv = 0, totalPktsSent = 0;
        long totalBytesRecv = 0, totalBytesSent = 0;
        long totalChunks = 0;

        for (ClientMetrics m : clientMetrics) {
            if (m.isConnected()) {
                success++;
                connectTimes.add((double) m.getConnectTimeMs());
                loginTimes.add((double) m.getLoginTimeMs());
                configTimes.add((double) m.getConfigurationTimeMs());
                fullJoinTimes.add((double) m.getFullJoinTimeMs());
                firstChunkTimes.add((double) m.getFirstChunkTimeMs());
                totalPktsRecv += m.getPacketsReceived();
                totalPktsSent += m.getPacketsSent();
                totalBytesRecv += m.getBytesReceived();
                totalBytesSent += m.getBytesSent();
                totalChunks += m.getChunksReceived();
                if (m.getKeepAliveRoundTrips() > 0) {
                    keepAliveTimes.add(m.getAvgKeepAliveMs());
                }
            } else {
                failed++;
            }
        }

        double avgConnect = connectTimes.stream().mapToDouble(d -> d).average().orElse(0);
        double avgLogin = loginTimes.stream().mapToDouble(d -> d).average().orElse(0);
        double avgConfig = configTimes.stream().mapToDouble(d -> d).average().orElse(0);
        double avgFullJoin = fullJoinTimes.stream().mapToDouble(d -> d).average().orElse(0);
        double avgFirstChunk = firstChunkTimes.stream().mapToDouble(d -> d).average().orElse(0);
        double avgKA = keepAliveTimes.stream().mapToDouble(d -> d).average().orElse(0);
        double maxKA = keepAliveTimes.stream().mapToDouble(d -> d).max().orElse(0);
        double p95KA = percentile(keepAliveTimes, 0.95);

        long[] gcPauses = gcPauseSamples.stream().mapToLong(a -> a[1]).toArray();
        long totalGcPause = 0;
        for (long p : gcPauses) totalGcPause += p;

        long[] heapArr = new long[heapSamples.size()];
        long peakHeap = 0;
        long sumHeap = 0;
        int idx = 0;
        for (Long h : heapSamples) {
            heapArr[idx++] = h;
            peakHeap = Math.max(peakHeap, h);
            sumHeap += h;
        }
        long avgHeap = heapSamples.isEmpty() ? 0 : sumHeap / heapSamples.size();

        return new PhaseResult(heapMB, durationMs, total, success, failed,
                avgConnect, avgLogin, avgConfig, avgFullJoin, avgFirstChunk,
                totalPktsRecv, totalPktsSent, totalBytesRecv, totalBytesSent,
                totalChunks, avgKA, maxKA, p95KA,
                gcPauses, totalGcPause, gcPauses.length,
                heapArr, peakHeap, avgHeap);
    }

    private static double percentile(List<Double> data, double p) {
        if (data.isEmpty()) return 0;
        List<Double> sorted = data.stream().sorted().collect(Collectors.toList());
        int idx = (int) Math.ceil(p * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }

    public List<ClientMetrics> getClientMetrics() {
        return Collections.unmodifiableList(clientMetrics);
    }
}
