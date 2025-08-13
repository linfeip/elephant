package com.linfp.elephant.controller;

import com.linfp.elephant.api.StatResponse;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@RestController
public class MetricsController {

    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsController.class);

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
            // 一个的耗时
            var perNano = (long) timer.totalTime(TimeUnit.NANOSECONDS) / count;
            var qps = 1E9 / perNano;
            var avg = Duration.ofNanos((long) timer.mean(TimeUnit.NANOSECONDS));

            var item = new StatResponse.StatItem();
            item.name = comment;
            item.p50 = p50.toString();
            item.p90 = p90.toString();
            item.p99 = p99.toString();
            item.count = count;
            item.avg = avg.toString();
            item.qps = 0;

            rsp.items.add(item);
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
