package com.linfp.elephant.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;

public class Metrics {
    private final MeterRegistry registry;

    public Metrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void update(Result result) {
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
    }

    public void clear(String runId) {
        var meters = registry.find("action_run_duration_seconds")
                .tag("runId", runId)
                .meters();
        for (var meter : meters) {
            registry.remove(meter);
        }
    }

    public static class Result {

        public String runId;

        public int code;

        public String error;

        public Duration elapsed;

        public String name;

        public String comment;
    }
}
