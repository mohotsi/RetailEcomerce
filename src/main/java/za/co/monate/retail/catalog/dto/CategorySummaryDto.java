package za.co.monate.retail.catalog.dto;

import lombok.Data;

@Data
public class CategorySummaryDto {
    private Long id;
    private String name;
    private String seoSlug;
}