package za.co.monate.retail.catalog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductImportDto {
    private String baseSku;
    private String productName;
    private String productDescription;
    private Set<String> categories; // e.g., ["all-products", "baking"]
    private String variantSku;
    private String attributeSummary;
    private BigDecimal price;
    private int stockQuantity;
}