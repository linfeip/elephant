package com.linfp.elephant.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;
import com.linfp.elephant.robot.ActionData;
import com.linfp.elephant.robot.HttpCallAction;
import com.linfp.elephant.robot.IAction;
import com.linfp.elephant.runner.RunnerManager;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;

@Configuration
public class AutoConfiguration {

    @Bean("http.call")
    public Function<ActionData, IAction> httpRequestAction(ObjectMapper om) {
        return data -> new HttpCallAction(data, om);
    }

    @Bean
    public Metrics metrics(MeterRegistry registry) {
        return new Metrics(registry);
    }

    @Bean
    public PrometheusMeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Bean
    public RunnerManager runnerManager(Map<String, Function<ActionData, IAction>> actionFactory, Metrics metrics) {
        return new RunnerManager(actionFactory, metrics);
    }
}
