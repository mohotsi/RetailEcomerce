package za.co.monate.retail.catalog.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BulkProductImportRequest {
    private String categorySlug;
    private String baseSku;
    private String name;
    private String description;
    private String imageUrl;
    private List<VariantDto> variants;


    @Data
    public static class VariantDto {
        private String sku;
        private String attributes;
        private BigDecimal price;
        private int stock;
    }
}