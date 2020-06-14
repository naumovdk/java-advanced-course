package ru.ifmo.rain.naumov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Math.*;


public class IterativeParallelism implements ScalarIP {
    private ParallelMapper mapper = null;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {}

    private <T, R> R computeParallel(int numberOfThreads, List<? extends T> values,
                                     Function<List<? extends T>, R> valueToResult,
                                     Function<List<? extends R>, R> collectResults) throws InterruptedException {

        if (numberOfThreads <= 0) {
            throw new IllegalArgumentException("number of threads <= 0");
        }
        numberOfThreads = min(numberOfThreads, values.size());
        List<Thread> threads = new ArrayList<>();

        List<List<? extends T>> segments = new ArrayList<>();
        final int segmentSize = (int) Math.ceil((double) values.size() / (double) numberOfThreads);
        int cutoff = values.size() % numberOfThreads;

        // вот здесь было плохо
        for (int i = 0, l = -1, r = 0; i < numberOfThreads && l < r; ++i) {
            l = min(r, values.size() - 1);
            r = min(l + segmentSize + (cutoff-- > 0 ? 1 : 0), values.size());
            segments.add(values.subList(l, r));
        }

        List<R> results;
        if (mapper == null) {
            results = new ArrayList<R>(Collections.nCopies(numberOfThreads, null));
            for (int i = 0; i < numberOfThreads; ++i) {
                final int index = i;
                threads.add(new Thread(() -> {
                    results.set(index, valueToResult.apply(segments.get(index)));
                }));
            }
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } else {
            results = mapper.map(valueToResult, segments);
        }

        return collectResults.apply(results);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("empty list");
        }
        return computeParallel(threads, values, (elements) -> Collections.max(elements, comparator),
                (results) -> Collections.max(results, comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return computeParallel(threads, values, (elements) -> elements.stream().allMatch(predicate),
                (results) -> results.stream().allMatch(Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return computeParallel(threads, values, (elements) -> elements.stream().anyMatch(predicate),
                (results) -> results.stream().anyMatch(Predicate.isEqual(true)));
    }
}