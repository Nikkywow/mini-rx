package com.minirx;

import com.minirx.Schedulers.IOThreadScheduler;
import com.minirx.Schedulers.SingleThreadScheduler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerTest {

    @Test
    public void testSubscribeOn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isNotMainThread = new AtomicBoolean(false);

        Observable.create((Observer<Object> observer) -> {
                    isNotMainThread.set(!Thread.currentThread().getName().equals("main"));
                    observer.onComplete();
                })
                .subscribeOn(new IOThreadScheduler())
                .subscribe(
                        v -> {},
                        e -> {},
                        latch::countDown
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(isNotMainThread.get());
    }

    @Test
    public void testObserveOn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isNotMainThread = new AtomicBoolean(false);

        Observable.create((Observer<Integer> observer) -> {
                    observer.onNext(1);
                    observer.onComplete();
                })
                .observeOn(new SingleThreadScheduler())
                .subscribe(
                        v -> isNotMainThread.set(!Thread.currentThread().getName().equals("main")),
                        e -> {},
                        latch::countDown
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(isNotMainThread.get());
    }
}