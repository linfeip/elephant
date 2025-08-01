package com.linfp.elephant.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.linfp.elephant.serializer.DurationDeserializer;

import java.time.Duration;
import java.util.List;

public record RunRequest(List<Action> actions, Robot robot) {

    public static class Action {
        public String action;

        @JsonDeserialize(using = DurationDeserializer.class)
        public Duration delay;

        public String data;

        public int timeout;

        public int loop;

        public String comment;
    }

    public static class Robot {
        public int num;
    }
}
