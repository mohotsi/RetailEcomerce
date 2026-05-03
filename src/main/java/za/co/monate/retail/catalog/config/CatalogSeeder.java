package za.co.monate.retail.catalog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import za.co.monate.retail.catalog.model.Category;
import za.co.monate.retail.catalog.repository.CategoryRepository;
import za.co.monate.retail.catalog.repository.ProductRepository;
import za.co.monate.retail.catalog.service.CatalogService;

import java.math.BigDecimal;
import java.util.Set;

/**
 * ============================================================================
 * CLASS: CatalogSeeder
 * PURPOSE: Implements CommandLineRunner, meaning Spring will automatically 
 * execute the run() method exactly once, immediately after the server starts.
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CatalogService catalogService;

    @Override
    public void run(String... args) throws Exception {
        // We only want to seed data if the database is completely empty.
        if (categoryRepository.count() == 0 && productRepository.count() == 0) {
            log.info("🌱 [SEEDER] Database is empty. Injecting enterprise catalog data...");
            seedCatalog();
            log.info("✅ [SEEDER] Catalog data successfully injected!");
        } else {
            log.info("⚡ [SEEDER] Catalog data already exists. Skipping injection.");
        }
    }

    private void seedCatalog() {
        // ---------------------------------------------------------
        // 1. BUILD THE CATEGORY HIERARCHY
        // ---------------------------------------------------------
        
        // Create the Parent Category
        Category groceries = new Category();
        groceries.setName("Groceries");
        groceries.setSeoSlug("groceries");
        categoryRepository.save(groceries); 

        // Create the Sub-Category
        Category beverages = new Category();
        beverages.setName("Beverages");
        beverages.setSeoSlug("groceries-beverages");
        beverages.setParentCategory(groceries); 
        categoryRepository.save(beverages);

        // --> NEW: Create the Summer Braai Category so Postman can find it! <--
        Category braai = new Category();
        braai.setName("Summer Braai Essentials");
        braai.setSeoSlug("summer-braai");
        categoryRepository.save(braai);

        // ---------------------------------------------------------
        // 2. BUILD THE BASE PRODUCT ("Oros Squash")
        // ---------------------------------------------------------
        catalogService.createCompleteProduct(
                // --> UPDATED: Oros now lives in both categories using Set.of <--
                Set.of("groceries-beverages", "summer-braai"), 
                "OROS-BASE-001",       
                "Oros Original Orange Squash", 
                "The classic South African orange squash. Just add water!"
        );

        // ---------------------------------------------------------
        // 3. ADD THE PHYSICAL VARIANTS (1L, 2L, 5L)
        // ---------------------------------------------------------
        
        catalogService.addVariantToProduct(
                "OROS-BASE-001",                
                "OROS-ORG-1L",                  
                "1 Litre Bottle",               
                new BigDecimal("22.99"),    
                150                             
        );

        catalogService.addVariantToProduct(
                "OROS-BASE-001",
                "OROS-ORG-2L",
                "2 Litre Bottle",
                new BigDecimal("39.99"),
                85
        );

        catalogService.addVariantToProduct(
                "OROS-BASE-001",
                "OROS-ORG-5L",
                "5 Litre Bulk Jug",
                new BigDecimal("89.99"),
                20
        );
    }
}