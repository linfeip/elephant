package com.linfp.elephant.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Metrics {
    private final MeterRegistry registry;

    private final ActionMetricsManager metricsManager = new ActionMetricsManager();

    public Metrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void update(Result result) {
        // 写入Prometheus 指标收集器
        Timer.builder("action_run_duration_seconds")
                .description("action run duration in seconds")
                .serviceLevelObjectives(
                        Duration.ofMillis(1),
                        Duration.ofMillis(3),
                        Duration.ofMillis(5),
                        Duration.ofMillis(7),
                        Duration.ofMillis(10),
                        Duration.ofMillis(20),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(200),
                        Duration.ofMillis(500),
                        Duration.ofMillis(750),
                        Duration.ofSeconds(1),
                        Duration.ofMillis(1200),
                        Duration.ofMillis(1500),
                        Duration.ofMillis(1750),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(3),
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(15),
                        Duration.ofSeconds(30)
                )
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(30))
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .tag("runId", result.runId)
                .tag("comment", result.comment)
                .register(registry)
                .record(result.elapsed);
        // 写入自定义的ActionMetricsManager指标收集
        metricsManager.update(result);
    }

    public void clear(String runId) {
        var meters = registry.find("action_run_duration_seconds")
                .tag("runId", runId)
                .meters();
        for (var meter : meters) {
            registry.remove(meter);
        }
    }

    public ActionMetricsManager getMetricsManager() {
        return metricsManager;
    }

    public static class Result {

        public String runId;

        public int code;

        public String error;

        public Duration elapsed;

        public String name;

        public String comment;
    }

    public static class ActionMetrics {
        public String runId;
        public String comment;
        public long startTime;
        public long lastTime;
        public AtomicLong count = new AtomicLong();
        public AtomicLong totalElapsed = new AtomicLong();

        public Duration avg() {
            if (count.get() == 0) {
                return Duration.ZERO;
            }
            return Duration.ofNanos(totalElapsed.get() / count.get());
        }

        public double tps() {
            if (count.get() == 0) {
                return 0.0;
            }
            var cost = lastTime - startTime;
            return (double) Duration.ofSeconds(1).toNanos() / (double) (cost / count.get());
        }
    }

    public static class ActionMetricsManager {
        private final Map<String, ActionMetrics> metrics = new HashMap<>();
        private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();

        public void update(Result result) {
            locker.readLock().lock();
            var key = result.runId + result.comment;
            var am = metrics.get(key);
            locker.readLock().unlock();
            if (am == null) {
                locker.writeLock().lock();
                // double check
                am = metrics.get(key);
                if (am == null) {
                    am = new ActionMetrics();
                    am.runId = result.runId;
                    am.comment = result.comment;
                    am.startTime = System.nanoTime();
                    metrics.put(key, am);
                }
                locker.writeLock().unlock();
            }

            am.count.incrementAndGet();
            am.totalElapsed.addAndGet(result.elapsed.toNanos());
            am.lastTime = System.nanoTime();
        }

        public List<ActionMetrics> find(String runId) {
            var results = new ArrayList<ActionMetrics>();
            for (var kv : metrics.entrySet()) {
                if (kv.getKey().startsWith(runId)) {
                    results.add(kv.getValue());
                }
            }
            return results;
        }
    }
}
