package com.zenon.tradeflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@Endpoint(id = "cacheContents") // Endpoint adı: /actuator/cacheContents
@RequiredArgsConstructor
public class CacheContentEndpoint {

    private final CacheManager cacheManager;

    @ReadOperation
    public Map<String, Object> getAllCacheContents() {
        // Tüm cache isimlerini dönüp içeriklerini bir map'te topluyoruz
        Map<String, Object> allContents = new java.util.HashMap<>();
        cacheManager.getCacheNames().forEach(name -> allContents.put(name, getCacheDetails(name)));
        return allContents;
    }

    @ReadOperation
    public Map<Object, Object> getCacheDetails(@Selector String name) {
        // Belirli bir cache ismine göre (/actuator/cacheContents/{name}) içerik döner
        var cache = cacheManager.getCache(name);
        if (cache instanceof CaffeineCache caffeineCache) {
            return caffeineCache.getNativeCache().asMap();
        }
        return Collections.singletonMap("error", "Sadece Caffeine cache desteklenmektedir.");
    }
}