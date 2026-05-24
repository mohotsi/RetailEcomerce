package za.co.monate.retail.catalog.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.co.monate.retail.catalog.service.CatalogService;

@Component
@RequiredArgsConstructor
public class CatalogRefreshScheduler {

    private final CatalogService catalogService;


    // Cron expression: "0 0/20 * * * *" runs at minute 0, 20, 40 of every hour
    @Scheduled(cron = "0 0/20 * * * *")
    public void refreshProductCache() {
        System.out.println("Refreshing product cache...");

        // This clears all entries in the "products" and "category_products" caches


        System.out.println("Cache refresh complete.");
    }
}