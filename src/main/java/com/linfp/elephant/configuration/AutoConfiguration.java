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
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class AutoConfiguration {

    private Server server;

    @Bean("http.call")
    public Function<ActionData, IAction> httpCallAction(ObjectMapper om, RestTemplate restTemplate) {
        return data -> new HttpCallAction(data, om, restTemplate);
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

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(1024);
        cm.setDefaultMaxPerRoute(1024);
        cm.closeIdle(TimeValue.ofSeconds(60));
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(15))
                .build();
        // 创建 HttpClient
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(60))
                .build();
    }

    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @PreDestroy
    public void onDestroy() {
        server.shutdown();
    }
}
