package com.linfp.elephant.controller;

import com.linfp.elephant.api.StatResponse;
import com.linfp.elephant.metrics.Metrics;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MetricsController {

    private final PrometheusMeterRegistry meterRegistry;

    private final Metrics metrics;

    public MetricsController(PrometheusMeterRegistry meterRegistry, Metrics metrics) {
        this.meterRegistry = meterRegistry;
        this.metrics = metrics;
    }

    @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
    public String prometheusMetrics() {
        return meterRegistry.scrape();
    }

    @GetMapping("/stats/{runId}")
    public StatResponse stats(@PathVariable String runId) {
        var rsp = new StatResponse();
        var actionMetrics = metrics.getMetricsManager().find(runId);
        for (var m : actionMetrics) {
            var errCount = m.errCount.get();
            var item = new StatResponse.StatItem();
            item.name = m.comment;
            item.count = m.count.get();
            item.errCount = errCount;
            item.avg = m.avg().toString();
            item.qps = m.tps();
            if (item.errCount > item.count) {
                item.errCount = item.count;
            }
            item.succCount = item.count - item.errCount;

            rsp.items.add(item);
        }
        return rsp;
    }
}
