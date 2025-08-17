package com.minirx;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ObservableTest {

    @Test
    public void testBasicSubscription() {
        List<String> received = new ArrayList<>();

        Observable.create((Observer<String> observer) -> {
            observer.onNext("Hello");
            observer.onNext("World");
            observer.onComplete();
        }).subscribe(
                received::add,
                error -> fail("Unexpected error"),
                () -> assertTrue(true)
        );

        assertEquals(Arrays.asList("Hello", "World"), received);
    }

    @Test
    public void testMapOperator() {
        List<Integer> lengths = new ArrayList<>();

        Observable.create((Observer<String> observer) -> {
                    observer.onNext("a");
                    observer.onNext("abc");
                    observer.onComplete();
                })
                .map(String::length)
                .subscribe(
                        lengths::add,
                        error -> fail(),
                        () -> assertEquals(2, lengths.size())
                );

        assertTrue(lengths.containsAll(Arrays.asList(1, 3)));
    }

    @Test
    public void testErrorHandling() {
        RuntimeException testError = new RuntimeException("test");
        AtomicReference<Throwable> receivedError = new AtomicReference<>();

        Observable.create((Observer<Object> observer) -> {
            observer.onError(testError);
        }).subscribe(
                item -> fail(),
                receivedError::set,
                () -> fail()
        );

        assertEquals(testError, receivedError.get());
    }
}