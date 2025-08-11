package com.linfp.elephant.controller;

import com.linfp.elephant.api.TestPostRequest;
import com.linfp.elephant.api.TestPostResponse;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
public class TestController {
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
        rsp.setName(req.getName());
        rsp.setAge(req.getAge());
        return rsp;
    }
}
