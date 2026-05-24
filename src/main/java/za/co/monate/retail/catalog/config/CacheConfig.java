/**
 * ============================================================================
 * FILE: CacheConfig.java
 * ARCHITECTURAL ROLE: Cache Manager Configuration
 * * * DEEP DIVE: WHY CAFFEINE?
 * Spring Boot's default cache is an in-memory Map. That's fine for small apps,
 * but for wholesale retail, we need an eviction policy. Caffeine is a high-
 * performance, near-optimal caching library. It uses the "Window TinyLfu"
 * algorithm, which automatically keeps the most "valuable" data (frequently
 * and recently accessed) in memory while evicting the "stale" data to save RAM.
 * ============================================================================
 */
package za.co.monate.retail.catalog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new
                CaffeineCacheManager("products", "categories", "catalog", "search");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500) // Don't allow cache to grow indefinitely
                .expireAfterWrite(10, TimeUnit.MINUTES)); // Refresh data every 10 mins
        return cacheManager;
    }
}