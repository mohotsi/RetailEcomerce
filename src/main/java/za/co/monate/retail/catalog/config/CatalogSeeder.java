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

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CatalogService catalogService;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0 && productRepository.count() == 0) {
            log.info("🌱 [SEEDER] Injecting enterprise catalog with B2B deal types...");
            seedCatalog();
            log.info("✅ [SEEDER] Catalog seeding complete.");
        }
    }

    private void seedCatalog() {
        // ---------------------------------------------------------
        // 1. FULLY POPULATED CATEGORY HIERARCHY
        // ---------------------------------------------------------

        // Root: Groceries
        Category groceries = createCategory("Groceries", "groceries", "Fresh and ambient food items", 1, null);

        // Child: Beverages (Parent is Groceries)
        createCategory("Beverages", "beverages", "Soft drinks, juices, and water", 1, groceries);

        // Child: Pantry (Parent is Groceries)
        createCategory("Pantry Staples", "pantry", "Flour, sugar, and dry grains", 2, groceries);

        // Root: Cleaning
        createCategory("Cleaning", "cleaning", "Household detergents and bulk chemicals", 2, null);

        // Root: Summer Braai
        createCategory("Summer Braai", "summer-braai", "Everything you need for the perfect South African braai", 3, null);

        // ---------------------------------------------------------
        // 2. PRODUCT DATA WITH B2B DEAL EXAMPLES
        // ---------------------------------------------------------

        // EXAMPLE A: STANDARD PRICING (Oros)
        catalogService.createCompleteProduct(
                Set.of("beverages", "summer-braai"),
                "OROS-BASE-001", "Oros Orange Squash", "Classic orange squash.",
                "https://upload.wikimedia.org/wikipedia/commons/5/5b/Orange_juice_1_edit1.jpg"
        );
        catalogService.addVariantToProduct("OROS-BASE-001", "OROS-1L", "1L Bottle", new BigDecimal("22.99"), 100);

        // EXAMPLE B: FIXED DISCOUNT (Ace Maize Meal)
        // Scenario: "R15.00 OFF" - The original price was R110.00
        catalogService.createCompleteProduct(
                Set.of("pantry"),
                "ACE-BASE-001", "Ace Super Maize Meal", "Special: R15 OFF bulk bags!",
                "https://upload.wikimedia.org/wikipedia/commons/f/fa/Sadza.jpg"
        );
        catalogService.addVariantToProduct("ACE-BASE-001", "ACE-10KG", "10kg Bag (Fixed Discount)", new BigDecimal("95.00"), 500);

        // EXAMPLE C: FIXED PERCENTAGE DISCOUNT (Selati Sugar)
        // Scenario: "10% OFF Trader Special"
        catalogService.createCompleteProduct(
                Set.of("pantry"),
                "SUG-BASE-001", "Selati White Sugar", "10% Discount applied at checkout for traders.",
                "https://upload.wikimedia.org/wikipedia/commons/3/3c/Sucre_blanc_cassonade_complet_rapadura.jpg"
        );
        catalogService.addVariantToProduct("SUG-BASE-001", "SUG-12KG", "12.5kg Bulk (10% Off)", new BigDecimal("245.50"), 200);

        // EXAMPLE D: BUNDLE DEAL / MULTI-BUY (Coca-Cola)
        // Scenario: "Buy 3 for R60.00" (Individual price is R25.00)
        catalogService.createCompleteProduct(
                Set.of("beverages", "summer-braai"),
                "COKE-BASE-001", "Coca-Cola Original", "Bundle Deal: Buy 3 cases for wholesale rates.",
                "https://upload.wikimedia.org/wikipedia/commons/c/ce/Coca-Cola_logo.svg"
        );
        catalogService.addVariantToProduct("COKE-BASE-001", "COKE-2L-CASE", "2L x 6 Case (Bundle)", new BigDecimal("125.00"), 300);
    }

    /**
     * Helper method to ensure all Category fields are filled to avoid 'null' in your Navigation JSON.
     */
    private Category createCategory(String name, String slug, String desc, int order, Category parent) {
        Category cat = new Category();
        cat.setName(name);
        cat.setSeoSlug(slug);
        cat.setDescription(desc);
        cat.setDisplayOrder(order);
        cat.setParentCategory(parent);
        return categoryRepository.save(cat);
    }
}