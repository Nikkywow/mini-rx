package com.minirx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final OnSubscribe<T> onSubscribe;

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public void subscribe(Observer<T> observer) {
        onSubscribe.call(observer);
    }

    // Добавляем методы для работы с Scheduler
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                scheduler.execute(() -> subscribe(observer))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        observer.onNext(mapper.apply(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        if (predicate.test(item)) {
                            observer.onNext(item);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(observer -> {
            List<Disposable> disposables = new ArrayList<>();
            subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    Observable<R> mapped = mapper.apply(item);
                    disposables.add(mapped.subscribe(
                            observer::onNext,
                            observer::onError,
                            () -> {}
                    ));
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

    public Disposable subscribe(
            Consumer<T> onNext,
            Consumer<Throwable> onError,
            Runnable onComplete
    ) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        };

        subscribe(observer);
        return () -> {};
    }

    public interface OnSubscribe<T> {
        void call(Observer<T> observer);
    }
}