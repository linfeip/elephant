package com.linfp.elephant.runner;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.metrics.Metrics;
import com.linfp.elephant.robot.ActionData;
import com.linfp.elephant.robot.IAction;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RunnerManager {

    private final Map<String, Function<ActionData, IAction>> actionFactory;

    private final Metrics metrics;

    private final Map<String, IRunner> runs = new ConcurrentHashMap<>();

    public RunnerManager(Map<String, Function<ActionData, IAction>> actionFactory, Metrics metrics) {
        this.actionFactory = actionFactory;
        this.metrics = metrics;
    }

    public String runAsync(RunRequest req, Runnable callback) {
        IRunner runner = new LocalRunner(actionFactory, metrics);
        Thread.startVirtualThread(() -> {
            runs.put(runner.runId(), runner);
            runner.run(req);
            runs.remove(runner.runId());
            if (callback != null) {
                callback.run();
            }
        });
        return runner.runId();
    }

    public String runAsync(RunRequest req) {
        return runAsync(req, null);
    }

    public void stop(String runId) {
        var runner = runs.remove(runId);
        if (runner != null) {
            runner.stop();
        }

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(Duration.ofMinutes(1));
            } catch (InterruptedException e) {
                //ignore
            }
            metrics.clear(runId);
        });
    }
}
