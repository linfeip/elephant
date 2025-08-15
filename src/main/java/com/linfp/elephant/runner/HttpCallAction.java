package com.linfp.elephant.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CancellationException;

public class HttpCallAction implements IAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpCallAction.class);

    private HttpArgs httpArgs;

    private boolean shouldParse = false;

    private final ActionData data;

    private final CloseableHttpClient httpClient;

    private final ObjectMapper om;

    public HttpCallAction(ActionData data, ObjectMapper om, CloseableHttpClient httpClient) {
        this.data = data;
        this.httpClient = httpClient;
        this.om = om;
        // 判断是否包含需要动态解析参数, 如果包含"#{", 那么设置shouldParse = true
        if (data.data != null && data.data.contains("#{")) {
            shouldParse = true;
        }
        // 没有动态解析的入参, 那么直接序列化成HttpArgs对象
        if (!shouldParse) {
            try {
                this.httpArgs = om.readValue(data.data, HttpArgs.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
            var parsedArgs = httpArgs;
            if (shouldParse) {
                var parsed = robot.getParserManager().parse(data.data, robot.getData());
                parsedArgs = om.readValue(parsed, HttpArgs.class);
            }

            ClassicHttpRequest req = new HttpUriRequestBase(parsedArgs.method.toUpperCase(), new URI(parsedArgs.url));
            if (parsedArgs.body != null) {
                req.setEntity(new StringEntity(parsedArgs.body));
            }
            if (parsedArgs.headers != null) {
                for (var kv : parsedArgs.headers.entrySet()) {
                    req.addHeader(kv.getKey(), kv.getValue());
                }
            }
            try (CloseableHttpResponse resp = httpClient.execute(req)) {
                if (resp.getCode() != HttpStatus.OK.value()) {
                    throw new RuntimeException("http call failed: " + resp.getCode());
                }

                var body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                try {
                    var bodyMap = om.readValue(body, new TypeReference<Map<String, Object>>() {
                    });
                    robot.setValue("resp", bodyMap);
                } catch (JsonParseException e) {
                    // 如果JSON解析失败, 那么就是一个纯文本
                    robot.setValue("resp", body);
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
