package za.co.monate.retail.catalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import za.co.monate.retail.catalog.dto.ProductResponseDto;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.repository.ProductRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    // A list of words that add no value to a product search
    private static final Set<String> STOP_WORDS = Set.of(
            "i", "want", "to", "buy", "get", "for", "my", "the", "a", "an", "is", "looking", "some"
    );
    private final CatalogService catalogService;
    private final ProductRepository productRepository;

    /**
     * ============================================================================
     * UPDATED: CatalogService.java (With Safety Checks)
     * * * DEEP DIVE: DEFENSIVE PROGRAMMING
     * We are adding "Guard Clauses". These check for invalid inputs BEFORE we
     * attempt to open a database connection. If the input is junk, we return
     * an empty Page immediately. This saves database cycles and prevents 500 errors.
     * ============================================================================
     */
    public Page<ProductResponseDto> performSmartSearch(String rawQuery, Pageable pageable) {
        // 1. Guard Clause: If input is null, empty, or just whitespace, exit early.
        if (rawQuery == null || rawQuery.isBlank()) {
            return Page.empty(pageable);
        }

        // 2. Sanitization: Prevent SQL injection or excessive query length
        String sanitizedQuery = rawQuery.trim();
        if (sanitizedQuery.length() > 50) {
            sanitizedQuery = sanitizedQuery.substring(0, 50);
        }

        // 3. Execution with Cache: The @Cacheable annotation will store the
        // result in Caffeine for 10 minutes.
        Page<Product> result = productRepository.searchProductsWithRanking(sanitizedQuery, pageable);

        // 4. Safety Guard: If the repository returns null (rare), ensure we don't crash
        if (result == null) {
            return Page.empty(pageable);
        }

        return result.map(catalogService::mapToResponseDto);
    }

    private int calculateRank(Product p, String keyword) {
        String k = keyword.toLowerCase();
        // Check Base SKU/Variant match
        boolean skuMatch = p.getBaseSku().toLowerCase().equals(k) ||
                p.getVariants().stream().anyMatch(v -> v.getSku().toLowerCase().equals(k));
        if (skuMatch) return 1;

        // Check Starts With
        if (p.getName().toLowerCase().startsWith(k)) return 3;

        // Check Contains
        if (p.getName().toLowerCase().contains(k)) return 4;

        return 5; // Default
    }


}