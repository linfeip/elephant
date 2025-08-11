package com.linfp.elephant.controller;

import com.linfp.elephant.api.StatResponse;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;


@RestController
@Slf4j
public class MetricsController {

    private final PrometheusMeterRegistry meterRegistry;

    public MetricsController(PrometheusMeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
    public String prometheusMetrics() {
        return meterRegistry.scrape();
    }

    @GetMapping("/stats/{runId}")
    public StatResponse stats(@PathVariable String runId) {
        var timers = meterRegistry.find("action_run_duration_seconds").tag("runId", runId).timers();

        var rsp = new StatResponse();

        for (var timer : timers) {
            var s = timer.takeSnapshot();

            var comment = timer.getId().getTag("comment");

            var p50 = getPercentileMillis(s, 0.5);
            var p90 = getPercentileMillis(s, 0.9);
            var p99 = getPercentileMillis(s, 0.99);

            var count = timer.count();

            var item = new StatResponse.StatItem();
            item.setName(comment);
            item.setP50(p50.toString());
            item.setP90(p90.toString());
            item.setP99(p99.toString());
            item.setCount(count);
            item.setQps(0);

            rsp.getItems().add(item);
        }

        return rsp;
    }

    private static Duration getPercentileMillis(HistogramSnapshot s, double p) {
        for (var v : s.percentileValues()) {
            if (v.percentile() == p) {
                return Duration.ofNanos((long) v.value());
            }
        }
        return Duration.ZERO;
    }
}
