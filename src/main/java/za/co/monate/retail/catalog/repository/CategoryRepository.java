package za.co.monate.retail.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.monate.retail.catalog.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // NEW: Find multiple categories at the exact same time
    Set<Category> findBySeoSlugIn(Set<String> seoSlugs);


    // 1. Find the target category by its SEO slug
    Optional<Category> findBySeoSlug(String seoSlug);

    // 2. Find all children categories that have this specific parent
    List<Category> findByParentCategory_Id(Long parentId);
    /**
     * Finds all categories that do NOT have a parent.
     * These are your main navigation items (Groceries, Electronics, etc.)
     */
    List<Category> findAllByParentCategoryIsNull();
}