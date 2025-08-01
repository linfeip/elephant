package com.linfp.elephant.runner;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.converter.Converter;
import com.linfp.elephant.metrics.Metrics;
import com.linfp.elephant.robot.ActionData;
import com.linfp.elephant.robot.IAction;
import com.linfp.elephant.robot.Robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class LocalRunner implements IRunner {

    private final Map<String, Function<ActionData, IAction>> actionFactory;

    private final Metrics metrics;

    private final List<Robot> robots = new ArrayList<>();

    private final String runId;

    public LocalRunner(Map<String, Function<ActionData, IAction>> actionFactory, Metrics metrics) {
        this.actionFactory = actionFactory;
        this.metrics = metrics;
        this.runId = UUID.randomUUID().toString();
    }

    @Override
    public void run(RunRequest config) {
        // 将配置中的Actions, 转成程序中的任务Actions
        List<IAction> actions = new ArrayList<>(config.actions().size());
        var curStep = 0;
        for (var act : config.actions()) {
            var makeFn = actionFactory.get(act.action);
            if (makeFn == null) {
                throw new RuntimeException("Unknown action: " + act.action);
            }

            var data = Converter.convert(act);
            data.setStep(curStep);

            var action = makeFn.apply(data);
            actions.add(action);

            curStep++;
        }

        // 构建Robot, Robot独立运行所有任务Actions
        var robot = config.robot();
        var latch = new CountDownLatch(robot.num);

        for (var i = 0; i < robot.num; i++) {
            var r = new Robot(runId, metrics, latch);
            robots.add(r);
            r.doLoop(actions);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        for (var t : robots) {
            t.shutdown();
        }
    }

    @Override
    public String runId() {
        return runId;
    }
}
