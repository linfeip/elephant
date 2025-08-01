package com.linfp.elephant.runner;

import com.linfp.elephant.api.RunRequest;

public interface IRunner {
    void run(RunRequest config);

    void stop();

    String runId();
}
