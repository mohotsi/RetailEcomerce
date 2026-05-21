package za.co.monate.retail.catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.catalog.CategoryImportRow;
import za.co.monate.retail.catalog.dto.*;
import za.co.monate.retail.catalog.model.Category;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.model.ProductVariant;
import za.co.monate.retail.catalog.repository.CategoryRepository;
import za.co.monate.retail.catalog.repository.ProductRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Gives us the 'log.info()' capability
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    // Inside CatalogService.java



// ... (Make sure this is inside your CatalogService class)

    /**
     * MAIN PIPELINE: Receives the JSON list and processes it safely.
     */
    @Transactional
    public void processBulkCategoriesFromJson(List<CategoryImportRow> rows) {
        // 1. First line of defense: Never process an empty request.
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("JSON payload cannot be empty");
        }

        try {
            // 2. The Stream Pipeline
            // It takes the raw JSON list, runs each item through our helper method below,
            // and then saves the result to the database.
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
     * THE HELPER METHOD: This is where the Existence Check actually lives!
     */
    private Category mapToCategoryEntity(CategoryImportRow row) {

        // --- THE EXISTENCE CHECK (UPSERT) ---
        // We ask the database: "Do you already have a category with this exact SEO slug?"
        // If YES -> It grabs the existing category so we can just update it.
        // If NO  -> The '.orElseGet' kicks in and creates a brand new, empty Category object.
        Category category = categoryRepository.findBySeoSlug(row.getSeoSlug())
                .orElseGet(Category::new);

        // --- UPDATE FIELDS ---
        // Because of the check above, we are either filling out a brand new form,
        // or just erasing and re-writing the values on an existing form.
        category.setName(row.getName());
        category.setSeoSlug(row.getSeoSlug());
        category.setDescription(row.getDescription());

        // --- NESTING/PARENT LOGIC ---
        // We check if the JSON gave us a parent slug (like "beverages")
        String parentSlug = row.getParentSlug();
        if (parentSlug != null && !parentSlug.trim().isEmpty()) {

            // We search the database for that parent...
            categoryRepository.findBySeoSlug(parentSlug.trim()).ifPresentOrElse(
                    // If we find it, link it!
                    parent -> category.setParentCategory(parent),

                    // If we don't find it, log a warning so the developer knows the JSON had a typo.
                    () -> log.warn("⚠️ Parent category '{}' not found for child '{}'", parentSlug, row.getSeoSlug())
            );
        }

        // We hand the fully built (or fully updated) object back to the stream pipeline to be saved.
        return category;
    }

    public ProductResponseDto mapToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setBaseSku(product.getBaseSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());

        // 1. Map the Categories Summary
        dto.setCategories(product.getCategories().stream().map(c -> {
            CategorySummaryDto cDto = new CategorySummaryDto();
            cDto.setId(c.getId());
            cDto.setName(c.getName());
            cDto.setSeoSlug(c.getSeoSlug());
            return cDto;
        }).collect(Collectors.toSet()));

        // 2. Map the Variants using your EXACT entity fields
        dto.setVariants(product.getVariants().stream().map(v -> {
            ProductVariantDto vDto = new ProductVariantDto();
            vDto.setSku(v.getSku());
            vDto.setAttributeSummary(v.getAttributeSummary()); // FIXED
            vDto.setPrice(v.getPrice());
            vDto.setStockQuantity(v.getStockQuantity());     // FIXED
            vDto.setImageUrl(v.getImageUrl());
            return vDto;
        }).collect(Collectors.toList()));

        // 3. Simple B2B Deal Type logic for the Frontend badges
        if (product.getDescription().contains("%")) {
            dto.setDealType("PERCENTAGE");
        } else if (product.getDescription().toLowerCase().contains("off")) {
            dto.setDealType("FIXED");
        } else {
            dto.setDealType("NONE");
        }

        return dto;
    }
    public Page<ProductResponseDto> getProductsByCategoryRecursive(String slug, Pageable pageable) {
        // 1. Find the category the user clicked on
        Category targetCategory = categoryRepository.findBySeoSlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found: " + slug));

        // 2. Start a list of IDs to search for
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(targetCategory.getId());

        // 3. Find all immediate children (e.g., if searching Groceries, find Beverages)
        List<Category> children = categoryRepository.findByParentCategory_Id(targetCategory.getId());

        // 4. Add children IDs to our search list
        List<Long> childrenIds = children.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        categoryIds.addAll(childrenIds);

        // 5. Query the database for any product in ANY of these categories
        return productRepository.findDistinctByCategories_IdIn(categoryIds, pageable)
                .map(this::mapToResponseDto);
    }

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


    /**
     * Reads the safe CSV, creates Base Products if they don't exist,
     * and attaches Variants to them.
     */
    @Transactional
    public void processBulkProductsImport(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip the header row
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                // Smart split: splits by commas EXCEPT when the comma is inside quotes
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Clean the data and remove any leftover CSV quotes
                String baseSku = data[0].replace("\"", "").trim();
                String productName = data[1].replace("\"", "").trim();
                String productDescription = data[2].replace("\"", "").trim();

                // The CSV has multiple categories separated by commas, so we split them into a Set
                String categoriesStr = data[3].replace("\"", "").trim();
                Set<String> categorySlugs = Set.of(categoriesStr.split("-,-")); // Update: Python saved them with a comma, let's just clean it

                // Note: data[4] is dealType. We skip it here because your mapToResponseDto
                // calculates it dynamically based on the description text!

                String variantSku = data[4].replace("\"", "").trim();
                String attributeSummary = data[5].replace("\"", "").trim();
                BigDecimal price = new BigDecimal(data[6].replace("\"", "").trim());
                int stockQuantity = Integer.parseInt(data[7].replace("\"", "").trim());

                // 1. Check if the Base Product already exists in the DB
                if (productRepository.findByBaseSku(baseSku).isEmpty()) {
                    // Split the categories (e.g. "all-products,beverages" -> Set["all-products", "beverages"])
                    Set<String> slugs = Set.of(categoriesStr.split(","));

                    // Use your existing method to create the parent!
                    createCompleteProduct(slugs, baseSku, productName, productDescription);
                }

                // 2. Add the physical variant to the parent product
                addVariantToProduct(baseSku, variantSku, attributeSummary, price, stockQuantity);
            }
            log.info("✅ Bulk Product Import Completed Successfully.");
        } catch (Exception e) {
            log.error("❌ Failed to process product import", e);
            throw new RuntimeException("Product CSV processing failed: " + e.getMessage());
        }
    }

    public void processBulkProductsFromJson(List<ProductImportDto> payload) {
        for (ProductImportDto dto : payload) {

            // 1. Check if the Base Product exists. If not, create it.
            if (productRepository.findByBaseSku(dto.getBaseSku()).isEmpty()) {
                createCompleteProduct(
                        dto.getCategories(),
                        dto.getBaseSku(),
                        dto.getProductName(),
                        dto.getProductDescription()
                );
            }

            // 2. Attach the physical variant to the base product
            addVariantToProduct(
                    dto.getBaseSku(),
                    dto.getVariantSku(),
                    dto.getAttributeSummary(),
                    dto.getPrice(),
                    dto.getStockQuantity()
            );
        }
        log.info("✅ Bulk JSON Product Import Completed. Processed {} records.", payload.size());
    }
    // Import this at the top if you haven't:
// import java.util.stream.Collectors;

    // 1. The main method called by the Controller
    public List<CategoryNodeDto> getNavigationTree() {
        // Fetch only the categories that have NO parent (the top level)
        List<Category> rootCategories = categoryRepository.findAllByParentCategoryIsNull();

        // Convert them into our clean DTO tree
        return rootCategories.stream()
                .map(this::mapToCategoryNode)
                .collect(Collectors.toList());
    }

    // 2. The recursive helper method
    private CategoryNodeDto mapToCategoryNode(Category category) {
        CategoryNodeDto node = new CategoryNodeDto();
        node.setName(category.getName());
        node.setSeoSlug(category.getSeoSlug());

        // RECURSION: We loop through the database children, and call THIS SAME METHOD
        // to map them into DTOs. This goes as deep as your tree goes!
        if (category.getSubCategories() != null) {
            List<CategoryNodeDto> children = category.getSubCategories().stream()
                    .map(this::mapToCategoryNode)
                    .collect(Collectors.toList());
            node.setSubCategories(children);
        }

        return node;
    }
}