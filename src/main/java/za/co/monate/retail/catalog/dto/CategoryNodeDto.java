package za.co.monate.retail.catalog.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryNodeDto {
    private String name;
    private String seoSlug;
    // Notice how it holds a list of ITSELF! This is how we build a tree.
    private List<CategoryNodeDto> subCategories = new ArrayList<>(); 
}