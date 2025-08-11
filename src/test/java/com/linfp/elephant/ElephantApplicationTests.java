package com.linfp.elephant;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.runner.RunnerManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

@SpringBootTest
class ElephantApplicationTests {

    @Autowired
    private RunnerManager runnerManager;

    @Test
    void contextLoads() {
    }

    @Test
    void httpAction() throws Exception {
        var action0 = new RunRequest.Action();
        action0.action = "http.call";
        action0.data = """
                {
                	"method": "GET",
                	"url": "http://localhost:9998/xxx"
                }
                """;
        action0.delay = Duration.ofSeconds(1);
        action0.timeout = Duration.ofSeconds(15);
        action0.comment = "GetXXX";
        action0.loop = 5;

        var action1 = new RunRequest.Action();
        action1.action = "http.call";
        action1.data = """
                {
                	"method": "POST",
                	"url": "http://localhost:9998/xxx",
                	"body": "{\\"a\\":1,\\"b\\":\\"2\\"}"
                }
                """;
        action1.delay = Duration.ofSeconds(1);
        action1.timeout = Duration.ofSeconds(15);
        action1.comment = "PostXXX";
        action1.loop = 5;

        var actions = List.of(action0, action1);

        var robot = new RunRequest.Robot();
        robot.num = 10_000;

        var config = new RunRequest();
        config.setActions(actions);
        config.setRobot(robot);

        var latch = new CountDownLatch(1);
        runnerManager.runAsync(config, latch::countDown);
        latch.await();
    }

    @Test
    void testVirtualSleep() throws Exception {
        for (var x = 0; x < 10; x++) {
            var num = 100_000;
            var latch = new CountDownLatch(num);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var start = System.currentTimeMillis();
                for (int i = 0; i < num; i++) {
                    executor.execute(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                latch.await();

                var elapsed = System.currentTimeMillis() - start;
                System.out.println(elapsed);
            }
        }
    }
}
