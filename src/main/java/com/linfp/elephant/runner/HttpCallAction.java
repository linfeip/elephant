package com.linfp.elephant.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class HttpCallAction implements IAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpCallAction.class);

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private static final Semaphore LIMITER = new Semaphore(1000);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private final HttpArgs httpArgs;

    private final ActionData data;

    public HttpCallAction(ActionData data, ObjectMapper om) {
        this.data = data;
        try {
            this.httpArgs = om.readValue(data.data, HttpArgs.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Metrics.Result doAction(Robot robot) throws InterruptedException {
        if (data.delay != null && !data.delay.isZero()) {
            Thread.sleep(data.delay);
        }

        LIMITER.acquire();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("starting http.call: {}", this.httpArgs);
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
            if (httpArgs.body != null && !httpArgs.body.isEmpty()) {
                bodyPublisher = HttpRequest.BodyPublishers.ofString(httpArgs.body);
                builder.header("Content-Type", "application/json");
            }

            builder.method(httpArgs.method, bodyPublisher);
            builder.uri(new URI(httpArgs.url));
            var timeout = DEFAULT_TIMEOUT;
            if (data.timeout != null && !data.timeout.isZero()) {
                timeout = data.timeout;
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
            result.code = 1;
            result.error = e.getMessage();
        } finally {
            var elapsed = System.nanoTime() - start;
            result.elapsed = Duration.ofNanos(elapsed);
            result.name = "http.call";
            result.comment = data.comment;
            LIMITER.release();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("finished http.call result: {}", result);
            }
        }

        return result;
    }

    @Override
    public int step() {
        return data.step;
    }

    @Override
    public ActionData getData() {
        return data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HttpArgs {
        public String url;
        public String method;
        public Map<String, String> headers;
        public String body;
        public int bodyType;
    }

    enum BodyType {
        None(0),
        FormData(1),
        XWwwFormUrlencoded(2);

        private final int code;

        BodyType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
