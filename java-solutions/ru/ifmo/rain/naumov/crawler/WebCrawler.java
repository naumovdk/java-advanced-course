package ru.ifmo.rain.naumov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newFixedThreadPool;

// java -cp . -p . -m info.kgeorgiy.java.advanced.crawler easy ru.ifmo.rain.naumov.crawler.WebCrawler

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloads;
    private final ExecutorService extracts;
    private final Map<String, IOException> errors = new ConcurrentHashMap<>();
    private final Set<String> downloaded = ConcurrentHashMap.newKeySet();
    private Set<String> nextLayer = ConcurrentHashMap.newKeySet();

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        if (downloader == null) {
            throw new IllegalArgumentException("null downloader passed");
        }
        this.downloader = downloader;
        this.downloads = newFixedThreadPool(downloaders);
        this.extracts = newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        nextLayer.add(url);
        while (depth-- > 0) {
            Set<String> currentLayer = nextLayer;
            nextLayer = ConcurrentHashMap.newKeySet();
            List<Future<Future<List<String>>>> extracted = new LinkedList<>();
            for (String link : currentLayer) {
                if (errors.containsKey(link)) {
                    continue;
                }
                extracted.add(downloads.submit(() -> {
                    try {
                        Document doc = downloader.download(link);
                        downloaded.add(link);
                        return extracts.submit(() -> {
                            try {
                                return doc.extractLinks();
                            } catch (IOException e) {
                                errors.put(link, e);
                            }
                            return null;
                        });
                    } catch (IOException e) {
                        errors.put(link, e);
                    }
                    return null;
                }));
            }
            for (var e : extracted) {
                try {
                    if (e != null && e.get() != null) {
                        e.get().get();
                    }
                } catch (InterruptedException | ExecutionException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            for (var e : extracted) {
                try {
                    if (e != null && e.get() != null) {
                        nextLayer.addAll(e.get().get());
                    }
                } catch (InterruptedException | ExecutionException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            nextLayer.removeAll(currentLayer);
            nextLayer.removeAll(downloaded);
        }
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public void close() {
        closePool(downloads);
        closePool(extracts);
    }

    private void closePool(ExecutorService pool) {
        pool.shutdownNow();
        try {
            pool.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("interrupted while waiting for shutdown");
        }
    }
}