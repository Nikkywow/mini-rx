package com.minirx;

public interface Scheduler {
    void execute(Runnable task);
}