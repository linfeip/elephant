package com.linfp.elephant.robot;

import lombok.Data;

import java.time.Duration;

@Data
public class ActionData {
    private String data;
    private int step;
    private int timeout;
    private Duration delay;
    private String comment;
    private int loop;
}
