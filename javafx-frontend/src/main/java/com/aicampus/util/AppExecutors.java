package com.aicampus.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class AppExecutors {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("app-worker-" + System.nanoTime());
        return t;
    });

    public static void submit(Runnable task) {
        EXECUTOR.execute(task);
    }

    public static <T> CompletableFuture<T> supplyAsync(Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
