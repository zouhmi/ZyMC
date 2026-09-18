package com.zouhmi.zymc.benchmark;

import com.zouhmi.zymc.benchmark.client.BenchmarkClient;
import com.zouhmi.zymc.benchmark.metrics.BenchmarkReport;
import com.zouhmi.zymc.benchmark.metrics.ClientMetrics;
import com.zouhmi.zymc.benchmark.metrics.MetricsCollector;
import com.zouhmi.zymc.benchmark.metrics.PhaseResult;
import com.zouhmi.zymc.core.ZMCVersion;
import com.zouhmi.zymc.network.protocol.ConnectionRegistry;
import com.zouhmi.zymc.network.server.LoginManager;
import com.zouhmi.zymc.network.server.MinecraftServerChannelInitializer;
import com.zouhmi.zymc.network.server.MinecraftServerHandler;
import com.zouhmi.zymc.world.FlatWorldGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class BenchmarkHarness {

    private static final int PORT = 25565;
    private static final int WARMUP_CLIENTS = 5;
    private static final int BENCH_CLIENTS = 50;
    private static final int CLIENT_RAMP_DELAY_MS = 200;
    private static final int PHASE_DURATION_MS = 30 * 60 * 1000;

    public static void main(String[] args) throws Exception {
        String reportDir = args.length > 0 ? args[0] : "benchmark/results";
        new File(reportDir).mkdirs();

        System.out.println("========================================");
        System.out.println("  ZyMC Benchmark Suite");
        System.out.println("  MC 1.21.11 | Protocol " + ZMCVersion.PROTOCOL_VERSION);
        System.out.println("  Phases: 1GB/30m, 2GB/30m, 4GB/30m");
        System.out.println("  Clients per phase: " + BENCH_CLIENTS);
        System.out.println("========================================");

        int[] heapSizes = {1024, 2048, 4096};
        List<PhaseResult> allResults = new ArrayList<>();

        for (int heapMB : heapSizes) {
            System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.printf("  PHASE: %d MB heap - Starting%n", heapMB);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");

            PhaseResult result = runPhase(heapMB, reportDir);
            allResults.add(result);

            System.out.printf("  Phase %d MB complete: %d/%d clients connected%n",
                    heapMB, result.getSuccessfulConnections(), result.getTotalClients());
            System.out.printf("  Avg join: %.1f ms | Avg chunk: %.1f ms | Peak heap: %d MB | GC: %.1f ms%n",
                    result.getAvgFullJoinTimeMs(), result.getAvgFirstChunkTimeMs(),
                    result.getPeakHeapUsed() / 1024 / 1024,
                    result.getTotalGcPauseNanos() / 1_000_000.0);

            Thread.sleep(5000);
        }

        String report = BenchmarkReport.generate(allResults);
        String reportPath = reportDir + "/benchmark_report.txt";
        BenchmarkReport.writeToFile(report, reportPath);
        System.out.println(report);
        System.out.println("Report saved to: " + reportPath);
    }

    private static PhaseResult runPhase(int heapMB, String reportDir) throws Exception {
        long phaseStart = System.currentTimeMillis();

        int port = PORT;
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        ConnectionRegistry serverRegistry = new ConnectionRegistry();
        FlatWorldGenerator worldGen = new FlatWorldGenerator();
        MinecraftServerHandler.Logger logger = msg -> {};

        MinecraftServerChannelInitializer initializer = new MinecraftServerChannelInitializer(
                serverRegistry, null, logger,
                reg -> {
                    LoginManager lm = new LoginManager(reg, null,
                            (x, z) -> worldGen.getChunk(x, z).toNetworkBytes(), false);
                    lm.setLogger(logger);
                    return lm;
                },
                null, null, null);
        initializer.registerHandshake();
        initializer.registerStatus();
        initializer.registerLogin();
        initializer.registerConfiguration();
        initializer.registerPlay();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer);
        bootstrap.bind("0.0.0.0", port).sync();
        System.out.printf("  Server started on port %d (offline mode)%n", port);

        Thread.sleep(2000);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        List<BenchmarkClient> clients = new CopyOnWriteArrayList<>();
        ExecutorService clientExecutor = Executors.newFixedThreadPool(20);

        System.out.printf("  Warmup: connecting %d clients%n", WARMUP_CLIENTS);
        MetricsCollector warmupCollector = new MetricsCollector();
        warmupCollector.start();
        for (int i = 0; i < WARMUP_CLIENTS; i++) {
            BenchmarkClient client = new BenchmarkClient("127.0.0.1", port, i, warmupCollector);
            clients.add(client);
            clientExecutor.submit(() -> {
                try { client.connect(); } catch (Exception ignored) {}
            });
            Thread.sleep(CLIENT_RAMP_DELAY_MS);
        }

        System.out.printf("  Waiting for warmup clients to stabilize...%n");
        Thread.sleep(10000);

        for (BenchmarkClient c : clients) {
            c.disconnect();
        }
        clients.clear();
        clientExecutor.shutdown();
        clientExecutor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.printf("  Benchmark: connecting %d clients%n", BENCH_CLIENTS);
        MetricsCollector collector = new MetricsCollector();
        collector.start();

        ScheduledExecutorService jvmSampler = Executors.newSingleThreadScheduledExecutor();
        long[] prevGcCount = new long[gcBeans.size()];
        long[] prevGcTime = new long[gcBeans.size()];
        for (int i = 0; i < gcBeans.size(); i++) {
            prevGcCount[i] = gcBeans.get(i).getCollectionCount();
            prevGcTime[i] = gcBeans.get(i).getCollectionTime();
        }
        jvmSampler.scheduleAtFixedRate(() -> {
            collector.sampleJVM(ProcessHandle.current().pid());
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            collector.recordHeapUsage(heapUsed);
            for (int i = 0; i < gcBeans.size(); i++) {
                long newCount = gcBeans.get(i).getCollectionCount();
                long newTime = gcBeans.get(i).getCollectionTime();
                if (newCount > prevGcCount[i]) {
                    collector.recordGcPause((newTime - prevGcTime[i]) * 1_000_000L);
                    prevGcCount[i] = newCount;
                    prevGcTime[i] = newTime;
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        clientExecutor = Executors.newFixedThreadPool(20);
        clients = new CopyOnWriteArrayList<>();

        for (int i = 0; i < BENCH_CLIENTS; i++) {
            BenchmarkClient client = new BenchmarkClient("127.0.0.1", port, i, collector);
            clients.add(client);
            clientExecutor.submit(() -> {
                try { client.connect(); } catch (Exception ignored) {}
            });
            Thread.sleep(CLIENT_RAMP_DELAY_MS);
        }

        long benchEnd = System.currentTimeMillis() + PHASE_DURATION_MS;
        int sampleCount = 0;
        while (System.currentTimeMillis() < benchEnd) {
            Thread.sleep(5000);
            sampleCount++;
            long elapsed = System.currentTimeMillis() - phaseStart;
            int connected = 0;
            for (BenchmarkClient c : clients) {
                if (c.getMetrics().isConnected()) connected++;
            }
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            System.out.printf("  [%dm %ds] connected=%d heap=%dMB samples=%d%n",
                    (int)(elapsed / 60000), (int)((elapsed % 60000) / 1000),
                    connected, heapUsed / 1024 / 1024, sampleCount);
        }

        for (BenchmarkClient c : clients) {
            c.disconnect();
        }
        clientExecutor.shutdown();
        clientExecutor.awaitTermination(10, TimeUnit.SECONDS);
        jvmSampler.shutdown();
        jvmSampler.awaitTermination(5, TimeUnit.SECONDS);

        collector.stop();
        PhaseResult result = collector.buildResult(heapMB);

        String phaseReport = BenchmarkReport.generate(List.of(result));
        BenchmarkReport.writeToFile(phaseReport, reportDir + "/phase_" + heapMB + "mb.txt");

        bossGroup.shutdownGracefully(0, 2, TimeUnit.SECONDS).sync();
        workerGroup.shutdownGracefully(0, 2, TimeUnit.SECONDS).sync();

        return result;
    }
}
