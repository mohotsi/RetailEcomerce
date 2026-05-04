package za.co.monate.retail.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * CLASS: CategoryImportRow
 * PURPOSE: A flat data structure used to map Excel/CSV rows to the Category entity.
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryImportRow {

    // The display name (e.g., "Ceylon Tea" or "OTC")
    private String name;

    // The unique identifier used in URLs (e.g., "food-cupboard-tea")
    private String seoSlug;

    // The slug of the parent category. 
    // If this is "food-cupboard", the current row becomes a sub-category.
    private String parentSlug;

    // Optional: Used for marketing or SEO blurbs
    private String description;

    // Optional: Helps with sorting in the frontend menu
    private Integer displayOrder;
}