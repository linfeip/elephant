package com.linfp.elephant.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CancellationException;

public class HttpCallAction implements IAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpCallAction.class);

    private final HttpArgs httpArgs;

    private final ActionData data;

    private final RestTemplate restTemplate;

    public HttpCallAction(ActionData data, ObjectMapper om, RestTemplate restTemplate) {
        this.data = data;
        this.restTemplate = restTemplate;
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

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("starting http.call: {}", this.httpArgs);
        }

        var result = new Metrics.Result();
        try {
            HttpEntity<?> ent = null;
            if (httpArgs.body != null || httpArgs.headers != null) {
                Object body = httpArgs.body;
                HttpHeaders headers = null;
                if (httpArgs.headers != null && !httpArgs.headers.isEmpty()) {
                    headers = new HttpHeaders();
                    for (var entry : httpArgs.headers.entrySet()) {
                        headers.add(entry.getKey(), entry.getValue());
                    }
                }
                ent = new HttpEntity<>(body, headers);
            }
            var resp = restTemplate.exchange(httpArgs.url, HttpMethod.valueOf(httpArgs.method.toUpperCase()), ent, String.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("http call failed: " + resp.getStatusCode());
            }
        } catch (CancellationException e) {
            throw e;
        } catch (Exception e) {
            result.code = 1;
            result.error = e.getMessage();
        } finally {
            result.end = System.nanoTime();
            result.name = "http.call";
            result.comment = data.comment;

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
