package za.co.monate.retail.catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.catalog.CategoryImportRow;
import za.co.monate.retail.catalog.model.Category;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.model.ProductVariant;
import za.co.monate.retail.catalog.repository.CategoryRepository;
import za.co.monate.retail.catalog.repository.ProductRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j // Gives us the 'log.info()' capability
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * @Transactional ensures ALL variants save successfully, or NONE of them do.
     */
    @Transactional
    public Product createCompleteProduct(Set<String> categorySlugs, String baseSku, String name, String description) {

        // 1. Fetch ALL requested categories from the database in one single query
        Set<Category> productCategories = categoryRepository.findBySeoSlugIn(categorySlugs);

        // Safety check: Did the merchandiser give us completely invalid categories?
        if (productCategories.isEmpty()) {
            throw new RuntimeException("None of the provided categories were found in the database!");
        }

        // 2. Build the Base Product using the new .categories() list
        Product baseProduct = Product.builder()
                .baseSku(baseSku)
                .name(name)
                .description(description)
                .categories(productCategories) // UPDATED: We now pass the List!
                .build();

        // 3. Save to memory/database
        return productRepository.save(baseProduct);
    }

    @Transactional
    public ProductVariant addVariantToProduct(String baseSku, String sku, String attributes, BigDecimal price, int stock) {

        // 1. Find the parent product
        Product baseProduct = productRepository.findByBaseSku(baseSku)
                .orElseThrow(() -> new RuntimeException("Base Product not found: " + baseSku));

        // 2. Build the physical variant
        ProductVariant variant = ProductVariant.builder()
                .sku(sku)
                .attributeSummary(attributes)
                .price(price)
                .stockQuantity(stock)
                .product(baseProduct) // Link it to the parent!
                .build();

        // 3. Add the variant to the parent's list
        baseProduct.getVariants().add(variant);

        // 4. Because of CascadeType.ALL on our entity, saving the base product
        // will automatically save the new variant down into the ProductVariant table!
        productRepository.save(baseProduct);

        log.info("Successfully added SKU {} to Base Product {}", sku, baseSku);

        return variant;
    }


    public void importCategoriesFromCsv(List<CategoryImportRow> rows) {
        for (CategoryImportRow row : rows) {
            Category category = categoryRepository.findBySeoSlug(row.getSeoSlug())
                    .orElse(new Category());

            category.setName(row.getName());
            category.setSeoSlug(row.getSeoSlug());

            // If a parent slug is provided, find it and link it
            if (row.getParentSlug() != null && !row.getParentSlug().isEmpty()) {
                Category parent = categoryRepository.findBySeoSlug(row.getParentSlug())
                        .orElseThrow(() -> new RuntimeException("Parent category " + row.getParentSlug() + " not found!"));
                category.setParentCategory(parent);
            }

            categoryRepository.save(category);
        }
    }
    @Transactional
    public void processBulkCategoriesFromJson(List<CategoryImportRow> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("JSON payload cannot be empty");
        }

        try {
            // The Stream Pipeline
            rows.stream()
                    .map(this::mapToCategoryEntity)
                    .forEach(categoryRepository::save);

            log.info("✅ Bulk JSON Category Import Completed. Processed {} records.", rows.size());
        } catch (Exception e) {
            log.error("❌ Failed to process JSON category import", e);
            throw new RuntimeException("JSON processing failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to map DTOs to Entities and handle DB lookups.
     */
    private Category mapToCategoryEntity(CategoryImportRow row) {
        // 1. Find existing or create new.
        // Using orElseGet(Category::new) is more efficient than orElse(new Category())
        Category category = categoryRepository.findBySeoSlug(row.getSeoSlug())
                .orElseGet(Category::new);

        category.setName(row.getName());
        category.setSeoSlug(row.getSeoSlug());
        category.setDescription(row.getDescription());

        // Handle null displayOrder safely
        if (row.getDisplayOrder() != null) {
            category.setDisplayOrder(row.getDisplayOrder());
        }

        // 2. Handle Parent Linking
        if (row.getParentSlug() != null && !row.getParentSlug().isEmpty()) {
            categoryRepository.findBySeoSlug(row.getParentSlug()).ifPresentOrElse(
                    category::setParentCategory,
                    () -> log.warn("⚠️ Parent category {} not found for child {}", row.getParentSlug(), row.getSeoSlug())
            );
        }

        return category;
    }

    public void processBulkCategoriesImport(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip the header row: name,seoSlug,parentSlug,description,displayOrder
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                // Map CSV columns to variables
                String name = data[0].trim();
                String seoSlug = data[1].trim();
                String parentSlug = (data.length > 2) ? data[2].trim() : "";
                String description = (data.length > 3) ? data[3].trim() : "";
                int displayOrder = (data.length > 4) ? Integer.parseInt(data[4].trim()) : 0;

                // 1. Check if category already exists to prevent duplicates
                Category category = categoryRepository.findBySeoSlug(seoSlug)
                        .orElse(new Category());

                category.setName(name);
                category.setSeoSlug(seoSlug);
                category.setDescription(description);
                category.setDisplayOrder(displayOrder);

                // 2. Handle Parent Linking
                if (!parentSlug.isEmpty()) {
                    categoryRepository.findBySeoSlug(parentSlug).ifPresentOrElse(
                            category::setParentCategory,
                            () -> log.warn("⚠️ Parent category {} not found for child {}", parentSlug, seoSlug)
                    );
                }

                categoryRepository.save(category);
            }
            log.info("✅ Bulk Category Import Completed.");
        } catch (Exception e) {
            log.error("❌ Failed to process category import", e);
            throw new RuntimeException("CSV processing failed: " + e.getMessage());
        }
    }
}