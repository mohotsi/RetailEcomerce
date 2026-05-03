package za.co.monate.retail.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.monate.retail.catalog.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySeoSlug(String seoSlug);
    
    // NEW: Find multiple categories at the exact same time
    Set<Category> findBySeoSlugIn(Set<String> seoSlugs);
}