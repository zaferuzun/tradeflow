package com.zenon.tradeflow.controller;

import com.zenon.tradeflow.service.PerformanceTest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class PerformanceTestController {


    private final PerformanceTest performanceTest;

    @GetMapping("/benchmark/platform")
    public String testPlatform(@RequestParam(defaultValue = "1000") int tasks) {
        // Geleneksel yöntem: Sabit 100 thread'lik havuz (Genelde sunucu limitleri böyledir)
        ExecutorService executor = Executors.newFixedThreadPool(100);
        long time = performanceTest.runPerformanceTest(tasks, executor);
        return "Platform Threads (Pool Size 100) ile " + tasks + " işlem süresi: " + time + " ms";
    }

    @GetMapping("/benchmark/virtual")
    public String testVirtual(@RequestParam(defaultValue = "1000") int tasks) {
        // Modern yöntem: Her iş için bir Sanal Thread
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        long time = performanceTest.runPerformanceTest(tasks, executor);
        return "Virtual Threads ile " + tasks + " işlem süresi: " + time + " ms";
    }
}
