package com.zenon.tradeflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PerformanceTest {

    public long runPerformanceTest(int taskCount, ExecutorService executor) {
        long startTime = System.currentTimeMillis();

        try (executor) {
            var futures = IntStream.range(0, taskCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            // Bir API çağrısını veya DB sorgusunu simüle ediyoruz (1 saniye bloklanma)
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, executor))
                    .toList();

            // Tüm görevlerin bitmesini bekle
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        return System.currentTimeMillis() - startTime;
    }
}
