package com.linfp.elephant.context;

import lombok.Data;

import java.util.concurrent.Semaphore;

@Data
public final class Context {
    private Semaphore httpLimiter;
}
