package com.linfp.elephant.runner;

import com.linfp.elephant.protocol.DynamicProto;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Robot {
    private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();

    private final Map<String, Object> data = new ConcurrentHashMap<>(16);

    private DynamicProto dynamicProto;

    private Thread th;

    private final LocalRunner runner;

    private final CountDownLatch latch;

    public Robot(LocalRunner runner, CountDownLatch latch) {
        this.runner = runner;
        this.latch = latch;
    }

    public void setValue(String key, Object value) {
        locker.writeLock().lock();
        data.put(key, value);
        locker.writeLock().unlock();
    }

    public Object getValue(String key) {
        locker.readLock().lock();
        var v = data.get(key);
        locker.readLock().unlock();
        return v;
    }

    public void delValue(String key) {
        locker.writeLock().lock();
        data.remove(key);
        locker.writeLock().unlock();
    }

    public void doLoop(List<IAction> actions) {
        th = Thread.startVirtualThread(() -> {
            try {
                var loop = runner.getLoop();
                var infinity = loop <= -1;
                while (loop > 0 || infinity) {
                    for (var action : actions) {
                        for (var i = 0; i < action.getData().loop; i++) {
                            var result = action.doAction(this);
                            result.runId = runner.runId();
                            runner.getMetrics().update(result);
                            // 释放协程CPU, 防止过忙
                            Thread.sleep(Duration.ZERO);
                        }
                    }
                    loop--;
                }
            } catch (InterruptedException | CancellationException e) {
                // ignore
            } finally {
                latch.countDown();
            }
        });
    }

    public void shutdown() {
        th.interrupt();
    }

    public DynamicProto getDynamicProto() {
        return dynamicProto;
    }

    public void setDynamicProto(DynamicProto dynamicProto) {
        this.dynamicProto = dynamicProto;
    }

    public LocalRunner getRunner() {
        return runner;
    }

    public ParserManager getParserManager() {
        return runner.getParserManager();
    }

    public Map<String, Object> getData() {
        return data;
    }
}
