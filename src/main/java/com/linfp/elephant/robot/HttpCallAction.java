package com.linfp.elephant.robot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Slf4j
public class HttpCallAction implements IAction {

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private static final Semaphore LIMITER = new Semaphore(1000);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private final HttpArgs httpArgs;

    private final ActionData data;

    public HttpCallAction(ActionData data, ObjectMapper om) {
        this.data = data;
        try {
            this.httpArgs = om.readValue(data.getData(), HttpArgs.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Metrics.Result doAction(Robot robot) throws InterruptedException {
        if (data.getDelay() != null && !data.getDelay().isZero()) {
            Thread.sleep(data.getDelay());
        }

        LIMITER.acquire();

        if (log.isDebugEnabled()) {
            log.debug("starting http.call: {}", this.httpArgs);
        }

        var result = new Metrics.Result();
        var start = System.nanoTime();
        try {
            var builder = HttpRequest.newBuilder();
            if (httpArgs.headers != null) {
                for (var kv : httpArgs.headers.entrySet()) {
                    builder.headers(kv.getKey(), kv.getValue());
                }
            }

            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
            if (httpArgs.getBody() != null && !httpArgs.getBody().isEmpty()) {
                bodyPublisher = HttpRequest.BodyPublishers.ofString(httpArgs.getBody());
                builder.header("Content-Type", "application/json");
            }

            builder.method(httpArgs.method, bodyPublisher);
            builder.uri(new URI(httpArgs.url));
            var timeout = DEFAULT_TIMEOUT;
            if (data.getTimeout() != null && !data.getTimeout().isZero()) {
                timeout = data.getTimeout();
            }
            builder.timeout(timeout);
            var req = builder.build();
            var resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != HttpStatus.OK.value()) {
                throw new RuntimeException("HTTP action failed: " + resp.statusCode());
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            result.setCode(1);
            result.setError(e.getMessage());
        } finally {
            var elapsed = System.nanoTime() - start;
            result.setElapsed(Duration.ofNanos(elapsed));
            result.setName("http.call");
            result.setComment(data.getComment());
            LIMITER.release();

            if (log.isDebugEnabled()) {
                log.debug("finished http.call result: {}", result);
            }
        }

        return result;
    }

    @Override
    public int step() {
        return data.getStep();
    }

    @Override
    public ActionData getData() {
        return data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HttpArgs {
        private String url;
        private String method;
        private Map<String, String> headers;
        private String body;
        private int bodyType;
    }

    @Getter
    enum BodyType {
        None(0),
        FormData(1),
        XWwwFormUrlencoded(2);

        private final int code;

        BodyType(int code) {
            this.code = code;
        }
    }
}
