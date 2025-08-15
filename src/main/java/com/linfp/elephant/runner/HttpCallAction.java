package com.linfp.elephant.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CancellationException;

public class HttpCallAction implements IAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpCallAction.class);

    private final HttpArgs httpArgs;

    private final ActionData data;

    private final CloseableHttpClient httpClient;

    public HttpCallAction(ActionData data, ObjectMapper om, CloseableHttpClient httpClient) {
        this.data = data;
        this.httpClient = httpClient;
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
            ClassicHttpRequest req = new HttpUriRequestBase(httpArgs.method.toUpperCase(), new URI(httpArgs.url));
            if (httpArgs.body != null) {
                req.setEntity(new StringEntity(httpArgs.body));
            }
            if (httpArgs.headers != null) {
                for (var kv : httpArgs.headers.entrySet()) {
                    req.addHeader(kv.getKey(), kv.getValue());
                }
            }
            try (CloseableHttpResponse resp = httpClient.execute(req)) {
                if (resp.getCode() != HttpStatus.OK.value()) {
                    throw new RuntimeException("http call failed: " + resp.getCode());
                }
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
