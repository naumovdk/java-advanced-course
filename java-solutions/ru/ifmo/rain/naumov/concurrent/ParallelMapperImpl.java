package ru.ifmo.rain.naumov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

// java -cp . -p . -m info.kgeorgiy.java.advanced.mapper scalar ru.ifmo.rain.naumov.concurrent.ParallelMapperImpl
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> queue;
    private List<Thread> threads;

    public ParallelMapperImpl(int numberOfThreads) {
        this.queue = new ArrayDeque<>();
        this.threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        compute();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        threads.forEach(Thread::start);
    }

    private void compute() throws InterruptedException {
        Runnable current;
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
            current = queue.poll();
            queue.notifyAll();
        }
        current.run();
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        AtomicInteger size = new AtomicInteger();

        for (int i = 0; i < args.size(); ++i) {
            int index = i;
            synchronized (queue) {
                queue.add(() -> {
                    result.set(index, f.apply(args.get(index)));
                    synchronized (size) {
                        size.getAndIncrement();
                        size.notifyAll();
                    }
                });
                queue.notifyAll();
            }
        }

        synchronized (size) {
            while (size.get() != args.size()) {
                size.wait();
            }
        }

        return result;
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
