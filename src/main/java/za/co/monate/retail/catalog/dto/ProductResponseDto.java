package za.co.monate.retail.catalog.dto;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class ProductResponseDto {
    private Long id;
    private String baseSku;
    private String name;
    private String description;
    private String imageUrl;
    private Set<CategorySummaryDto> categories;
    private List<ProductVariantDto> variants;
    
    // B2B Logic Helpers
    private String dealType; // "FIXED", "PERCENTAGE", "BUNDLE", "NONE"
    private String dealDescription;
}