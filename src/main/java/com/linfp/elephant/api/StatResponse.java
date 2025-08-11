package com.linfp.elephant.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StatResponse {

    private List<StatItem> items = new ArrayList<>();

    @Data
    public static class StatItem {
        private String name;

        private String p99;

        private String p90;

        private String p50;

        private Long count;

        private String avg;

        private Integer qps;
    }
}
