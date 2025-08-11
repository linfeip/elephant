package com.linfp.elephant.robot;

import com.linfp.elephant.metrics.Metrics;

public interface IAction {
    Metrics.Result doAction(Robot robot) throws InterruptedException;

    int step();

    ActionData getData();
}
