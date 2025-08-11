package com.linfp.elephant.robot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linfp.elephant.metrics.Metrics;

public class GrpcCallAction implements IAction {
    private final ActionData data;

    public GrpcCallAction(ActionData data, ObjectMapper om) {
        this.data = data;
    }

    @Override
    public Metrics.Result doAction(Robot robot) throws InterruptedException {
        return new Metrics.Result();
    }

    @Override
    public int step() {
        return data.getStep();
    }

    @Override
    public ActionData getData() {
        return data;
    }
}
