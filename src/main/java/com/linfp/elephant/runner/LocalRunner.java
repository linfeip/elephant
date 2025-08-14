package com.linfp.elephant.runner;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.converter.Converter;
import com.linfp.elephant.metrics.Metrics;
import com.linfp.elephant.protocol.DynamicProto;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class LocalRunner implements IRunner {
    private static final Logger logger = LoggerFactory.getLogger(LocalRunner.class);

    private final Map<String, Function<ActionData, IAction>> actionFactory;

    private final Metrics metrics;

    private final List<Robot> robots = new ArrayList<>();

    private final String runId;

    private final Map<String, ManagedChannel> grpcChannels = new HashMap<>();
    private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();

    private int loop;

    public LocalRunner(Map<String, Function<ActionData, IAction>> actionFactory, Metrics metrics) {
        this.actionFactory = actionFactory;
        this.metrics = metrics;
        this.runId = UUID.randomUUID().toString();
    }

    @Override
    public void run(RunRequest config) {
        logger.info("starting run");

        loop = config.loop;
        if (config.loop == 0) {
            loop = 1;
        }

        var dynamicProto = new DynamicProto();
        if (config.protos != null) {
            // 动态解析gRPC proto文件
            for (String proto : config.protos) {
                dynamicProto.register(new ByteArrayInputStream(proto.getBytes()));
            }
        }

        // 将配置中的Actions, 转成程序中的任务Actions
        List<IAction> actions = new ArrayList<>(config.actions.size());
        var curStep = 0;
        for (var act : config.actions) {
            var makeFn = actionFactory.get(act.action);
            if (makeFn == null) {
                throw new RuntimeException("Unknown action: " + act.action);
            }

            var data = Converter.convert(act);
            data.step = curStep;

            var action = makeFn.apply(data);
            actions.add(action);

            curStep++;
        }

        // 构建Robot, Robot独立运行所有任务Actions
        var robot = config.robot;
        var start = System.nanoTime();
        var latch = new CountDownLatch(robot.num);

        for (var i = 0; i < robot.num; i++) {
            var r = new Robot(this, latch);
            r.setDynamicProto(dynamicProto);
            robots.add(r);
            r.doLoop(actions);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var elapsed = System.nanoTime() - start;
        logger.info("finished run elapsed time: {}", Duration.ofNanos(elapsed));

        clear();
    }

    @Override
    public void stop() {
        for (var t : robots) {
            t.shutdown();
        }
        clear();
    }

    private void clear() {
        locker.writeLock().lock();
        try {
            grpcChannels.values().forEach(ManagedChannel::shutdown);
            grpcChannels.clear();
        } finally {
            locker.writeLock().unlock();
        }

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(Duration.ofMinutes(30));
            } catch (InterruptedException e) {
                //ignore
            }
            metrics.clear(runId);
        });
    }

    @Override
    public String runId() {
        return runId;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public ManagedChannel getChannel(String addr) {
        locker.readLock().lock();
        try {
            return grpcChannels.get(addr);
        } finally {
            locker.readLock().unlock();
        }
    }

    public void setChannel(String addr, ManagedChannel channel) {
        locker.writeLock().lock();
        try {
            grpcChannels.putIfAbsent(addr, channel);
        } finally {
            locker.writeLock().unlock();
        }
    }

    public int getLoop() {
        return loop;
    }
}
