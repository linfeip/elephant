package com.linfp.elephant.controller;

import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.runner.RunnerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunnerController {

    @Autowired
    private RunnerManager runnerManager;

    @PostMapping("saveConfig")
    public String saveConfig() {
        return "OK";
    }

    @PostMapping("run")
    public String run(@Validated @RequestBody RunRequest req) {
        return runnerManager.runAsync(req);
    }

    @PostMapping("stop/{id}")
    public void stop(@PathVariable String id) {
        runnerManager.stop(id);
    }
}
