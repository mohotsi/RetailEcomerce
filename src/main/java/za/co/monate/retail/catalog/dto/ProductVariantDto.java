package za.co.monate.retail.catalog.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantDto {
    private String sku;
    private String attributeSummary; // Matches your Entity field
    private BigDecimal price;
    private Integer stockQuantity;   // Matches your Entity field
    private String imageUrl;
}