package com.linfp.elephant.api;

import java.util.ArrayList;
import java.util.List;

public class StatResponse {

    public List<StatItem> items = new ArrayList<>();

    public static class StatItem {
        public String name;

        public String p99;

        public String p90;

        public String p50;

        public Long count;

        public String avg;

        public Double qps;
    }
}
