package za.co.monate.retail.catalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.monate.retail.catalog.dto.ProductResponseDto;
import za.co.monate.retail.catalog.dto.ProductVariantDto;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.repository.ProductRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final CatalogService catalogService;
    
    private final ProductRepository productRepository;

    // A list of words that add no value to a product search
    private static final Set<String> STOP_WORDS = Set.of(
        "i", "want", "to", "buy", "get", "for", "my", "the", "a", "an", "is", "looking", "some"
    );



    public List<ProductResponseDto> performSmartSearch(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Clean the sentence. Convert to lowercase and split by spaces.
        String[] words = rawQuery.toLowerCase().split("\\s+");
        
        // 2. Find the most "important" word (ignore the stop words)
        String coreKeyword = "";
        for (String word : words) {
            if (!STOP_WORDS.contains(word)) {
                coreKeyword = word; // E.g., we find "milk" and ignore "i want to"
                break; // For this iteration, we just grab the first important noun
            }
        }

        // 3. Send the clean keyword to our ranked database query
        return productRepository.searchProductsWithRanking(rawQuery).stream()
                .map(catalogService::mapToResponseDto).toList();
    }
}