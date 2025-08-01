package com.linfp.elephant.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public class DurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JacksonException {
        var v = p.getText();
        if (v == null || v.length() < 2) {
            throw new JsonParseException(p, "Missing value");
        }

        var msIdx = v.indexOf("ms");
        if (msIdx > 0) {
            var ss = v.substring(0, msIdx);
            return Duration.ofMillis(Long.parseLong(ss));
        }

        var secIdx = v.indexOf("s");
        if (secIdx > 0) {
            var ss = v.substring(0, secIdx);
            return Duration.ofSeconds(Long.parseLong(ss));
        }

        var minIdx = v.indexOf("m");
        if (minIdx > 0) {
            var ss = v.substring(0, minIdx);
            return Duration.ofMinutes(Long.parseLong(ss));
        }

        var hrIdx = v.indexOf("h");
        if (hrIdx > 0) {
            var ss = v.substring(0, hrIdx);
            return Duration.ofHours(Long.parseLong(ss));
        }

        throw new JsonParseException(p, "Duration format error");
    }
}
