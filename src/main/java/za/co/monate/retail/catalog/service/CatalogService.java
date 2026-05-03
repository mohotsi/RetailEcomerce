package za.co.monate.retail.catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.monate.retail.catalog.model.Category;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.model.ProductVariant;
import za.co.monate.retail.catalog.repository.CategoryRepository;
import za.co.monate.retail.catalog.repository.ProductRepository;

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
}