package com.linfp.elephant.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.grpc.GreeterGrpcService;
import com.linfp.elephant.metrics.Metrics;
import com.linfp.elephant.runner.*;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class AutoConfiguration {

    private Server server;

    @Bean("http.call")
    public Function<ActionData, IAction> httpCallAction(ObjectMapper om) {
        return data -> new HttpCallAction(data, om);
    }

    @Bean("grpc.call")
    public Function<ActionData, IAction> grpcCallAction(ObjectMapper om) {
        return data -> new GrpcCallAction(data, om);
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

    @Bean
    public Server grpcServer(@Value("${grpc.port:8081}") Integer port) throws IOException {
        // 用于grpc greeter测试
        this.server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new GreeterGrpcService())
                .build()
                .start();
        return server;
    }

    @PreDestroy
    public void onDestroy() {
        server.shutdown();
    }
}
