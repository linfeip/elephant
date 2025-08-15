package com.linfp.elephant.controller;

import com.linfp.elephant.api.TestPostRequest;
import com.linfp.elephant.api.TestPostResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class TestController {
    private final AtomicLong seq = new AtomicLong(1 << 10);

    @RequestMapping("ping")
    public String ping(@RequestParam(defaultValue = "0") Integer delay) {
        if (delay != null && delay > 0) {
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return "pong";
    }

    @PostMapping("testPost")
    public TestPostResponse testPost(@RequestBody TestPostRequest req) {
        var rsp = new TestPostResponse();
        rsp.name = req.name;
        rsp.age = req.age;
        return rsp;
    }

    @PostMapping("testLogin")
    public Map<String, Long> testLogin() {
        return Map.of("token", seq.incrementAndGet());
    }

    @PostMapping("testUserInfo")
    public Map<String, Object> testUserInfo(@RequestBody Map<String, Object> req) {
        return Map.of("username", "testUser", "token", req.get("token"));
    }

    @PostMapping("testLogout")
    public Map<String, Object> testLogout(@RequestBody Map<String, Object> req) {
        return Map.of("time", System.nanoTime(), "user", req.get("username"));
    }
}
