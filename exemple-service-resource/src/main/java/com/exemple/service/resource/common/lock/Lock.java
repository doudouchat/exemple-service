package com.exemple.service.resource.common.lock;

import java.util.List;

import org.apache.curator.framework.recipes.locks.InterProcessLock;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class Lock {

    private final List<? extends InterProcessLock> locks;

    public void execute(Runnable action) {
        try {
            locks.forEach(Lock::acquire);
            action.run();
        } finally {
            locks.forEach(Lock::release);
        }
    }

    @SneakyThrows
    private static void acquire(InterProcessLock lock) {
        lock.acquire();
    }

    @SneakyThrows
    private static void release(InterProcessLock lock) {
        lock.release();
    }

}
