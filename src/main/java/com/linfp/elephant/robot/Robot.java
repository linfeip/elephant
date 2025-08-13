package com.linfp.elephant.robot;

import com.linfp.elephant.metrics.Metrics;
import com.linfp.elephant.protocol.DynamicProto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Robot {
    private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();

    private final Map<String, Object> data = new ConcurrentHashMap<>(16);

    private DynamicProto dynamicProto;

    private final Metrics metrics;

    private final CountDownLatch latch;

    private Thread th;

    private final String runId;

    public Robot(String runId, Metrics metrics, CountDownLatch latch) {
        this.runId = runId;
        this.metrics = metrics;
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
                for (var action : actions) {
                    for (var i = 0; i < action.getData().getLoop(); i++) {
                        var result = action.doAction(this);
                        result.setRunId(runId);
                        metrics.update(result);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
}
