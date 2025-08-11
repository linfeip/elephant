package com.linfp.elephant.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.linfp.elephant.serializer.DurationDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class RunRequest {

    @NotEmpty(message = "actions not empty")
    private List<Action> actions;

    @NotNull(message = "robot not empty")
    private Robot robot;

    public static class Action {
        public String action;

        @JsonDeserialize(using = DurationDeserializer.class)
        public Duration delay;

        @NotBlank
        public String data;

        @JsonDeserialize(using = DurationDeserializer.class)
        public Duration timeout;

        public int loop;

        public String comment;
    }

    public static class Robot {
        public int num;
    }
}
