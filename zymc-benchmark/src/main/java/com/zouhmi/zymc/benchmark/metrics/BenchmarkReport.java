package com.zouhmi.zymc.benchmark.metrics;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public final class BenchmarkReport {

    public static String generate(List<PhaseResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append("  ZyMC BENCHMARK REPORT - 1.21.11 (Protocol 774)\n");
        sb.append("=".repeat(80)).append("\n\n");

        sb.append(String.format("  Total benchmark time: %.1f minutes\n\n",
                results.stream().mapToLong(PhaseResult::getDurationMs).sum() / 60_000.0));

        for (PhaseResult r : results) {
            appendPhaseReport(sb, r);
        }

        if (results.size() >= 2) {
            appendComparison(sb, results);
        }

        sb.append("=".repeat(80)).append("\n");
        return sb.toString();
    }

    private static void appendPhaseReport(StringBuilder sb, PhaseResult r) {
        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("  PHASE: %d MB Heap - Duration: %.1f minutes\n",
                r.getHeapMB(), r.getDurationMs() / 60_000.0));
        sb.append("-".repeat(80)).append("\n\n");

        sb.append("  CONNECTION METRICS\n");
        sb.append("  ").append("-".repeat(40)).append("\n");
        sb.append(String.format("    Total clients:          %d\n", r.getTotalClients()));
        sb.append(String.format("    Successful:             %d (%.1f%%)\n",
                r.getSuccessfulConnections(),
                r.getTotalClients() == 0 ? 0 : (double) r.getSuccessfulConnections() / r.getTotalClients() * 100));
        sb.append(String.format("    Failed:                 %d\n", r.getFailedConnections()));
        sb.append(String.format("    Avg connect time:       %.2f ms\n", r.getAvgConnectTimeMs()));
        sb.append(String.format("    Avg login time:         %.2f ms\n", r.getAvgLoginTimeMs()));
        sb.append(String.format("    Avg config time:        %.2f ms\n", r.getAvgConfigurationTimeMs()));
        sb.append(String.format("    Avg full join time:     %.2f ms\n", r.getAvgFullJoinTimeMs()));
        sb.append(String.format("    Avg first chunk time:   %.2f ms\n", r.getAvgFirstChunkTimeMs()));
        sb.append("\n");

        sb.append("  THROUGHPUT\n");
        sb.append("  ").append("-".repeat(40)).append("\n");
        sb.append(String.format("    Total packets recv:     %,d\n", r.getTotalPacketsReceived()));
        sb.append(String.format("    Total packets sent:     %,d\n", r.getTotalPacketsSent()));
        sb.append(String.format("    Total bytes recv:       %,d\n", r.getTotalBytesReceived()));
        sb.append(String.format("    Total bytes sent:       %,d\n", r.getTotalBytesSent()));
        sb.append(String.format("    Total chunks received:  %,d\n", r.getTotalChunksReceived()));
        sb.append(String.format("    Packet throughput:      %,.1f pps\n", r.getThroughputPps()));
        sb.append(String.format("    Byte throughput:        %,.0f B/s\n", r.getThroughputBps()));
        sb.append("\n");

        sb.append("  LATENCY (KeepAlive RTT)\n");
        sb.append("  ").append("-".repeat(40)).append("\n");
        sb.append(String.format("    Average:                %.2f ms\n", r.getAvgKeepAliveMs()));
        sb.append(String.format("    Max:                    %.2f ms\n", r.getMaxKeepAliveMs()));
        sb.append(String.format("    P95:                    %.2f ms\n", r.getP95KeepAliveMs()));
        sb.append("\n");

        sb.append("  JVM / MEMORY\n");
        sb.append("  ").append("-".repeat(40)).append("\n");
        sb.append(String.format("    Heap configured:        %d MB\n", r.getHeapMB()));
        sb.append(String.format("    Peak heap used:         %,d MB\n", r.getPeakHeapUsed() / 1024 / 1024));
        sb.append(String.format("    Avg heap used:          %,d MB\n", r.getAvgHeapUsed() / 1024 / 1024));
        sb.append(String.format("    GC count:               %d\n", r.getGcCount()));
        sb.append(String.format("    Total GC pause:         %.1f ms\n", r.getTotalGcPauseNanos() / 1_000_000.0));
        sb.append(String.format("    GC pause percent:       %.3f%%\n", r.getGcPausePercent()));
        sb.append("\n\n");
    }

    private static void appendComparison(StringBuilder sb, List<PhaseResult> results) {
        sb.append("=".repeat(80)).append("\n");
        sb.append("  COMPARISON ACROSS PHASES\n");
        sb.append("=".repeat(80)).append("\n\n");

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "Metric", results.get(0).getHeapMB() + " MB",
                results.size() > 1 ? results.get(1).getHeapMB() + " MB" : "N/A",
                results.size() > 2 ? results.get(2).getHeapMB() + " MB" : "N/A"));
        sb.append("  ").append("-".repeat(57)).append("\n");

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "Join (ms)",
                fmt(results.get(0).getAvgFullJoinTimeMs()),
                results.size() > 1 ? fmt(results.get(1).getAvgFullJoinTimeMs()) : "N/A",
                results.size() > 2 ? fmt(results.get(2).getAvgFullJoinTimeMs()) : "N/A"));

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "Chunk (ms)",
                fmt(results.get(0).getAvgFirstChunkTimeMs()),
                results.size() > 1 ? fmt(results.get(1).getAvgFirstChunkTimeMs()) : "N/A",
                results.size() > 2 ? fmt(results.get(2).getAvgFirstChunkTimeMs()) : "N/A"));

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "KA RTT (ms)",
                fmt(results.get(0).getAvgKeepAliveMs()),
                results.size() > 1 ? fmt(results.get(1).getAvgKeepAliveMs()) : "N/A",
                results.size() > 2 ? fmt(results.get(2).getAvgKeepAliveMs()) : "N/A"));

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "PeakHeap(MB)",
                String.valueOf(results.get(0).getPeakHeapUsed() / 1024 / 1024),
                results.size() > 1 ? String.valueOf(results.get(1).getPeakHeapUsed() / 1024 / 1024) : "N/A",
                results.size() > 2 ? String.valueOf(results.get(2).getPeakHeapUsed() / 1024 / 1024) : "N/A"));

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "GC Pauses",
                fmt(results.get(0).getTotalGcPauseNanos() / 1_000_000.0),
                results.size() > 1 ? fmt(results.get(1).getTotalGcPauseNanos() / 1_000_000.0) : "N/A",
                results.size() > 2 ? fmt(results.get(2).getTotalGcPauseNanos() / 1_000_000.0) : "N/A"));

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "GC%%",
                String.format("%.3f%%", results.get(0).getGcPausePercent()),
                results.size() > 1 ? String.format("%.3f%%", results.get(1).getGcPausePercent()) : "N/A",
                results.size() > 2 ? String.format("%.3f%%", results.get(2).getGcPausePercent()) : "N/A"));

        sb.append(String.format("  %-12s %-15s %-15s %-15s\n", "Packets/s",
                fmt(results.get(0).getThroughputPps()),
                results.size() > 1 ? fmt(results.get(1).getThroughputPps()) : "N/A",
                results.size() > 2 ? fmt(results.get(2).getThroughputPps()) : "N/A"));

        sb.append("\n");
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }

    public static void writeToFile(String report, String path) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.print(report);
        }
    }
}
