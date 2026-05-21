package za.co.monate.retail.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.monate.retail.catalog.dto.ProductVariantDto;
import za.co.monate.retail.catalog.model.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // We look up base products by their Base SKU, not their database ID
    Optional<Product> findByBaseSku(String baseSku);

    /**
     * ========================================================================
     * SPRING DATA JPA PROPERTY TRAVERSAL
     * ========================================================================
     * findBy -> Starts the query
     * Categories -> Looks at the List<Category> or Set<Category> in Product
     * _ (Underscore) -> Tells Spring to "step inside" the Category object
     * SeoSlug -> Matches the EXACT variable name 'seoSlug' in your Category entity
     * ========================================================================
     */
    Page<Product> findByCategories_SeoSlug(String seoSlug, Pageable pageable);

    /**
     * findDistinctByCategories_IdIn
     * ----------------------------
     * findDistinctBy -> Ensures no duplicate products in results.
     * Categories_Id -> Steps into the Category set and looks at the ID.
     * In -> Allows us to pass a List of IDs [1, 2, 5, etc.]
     */
    Page<Product> findDistinctByCategories_IdIn(Collection<Long> categoryIds, Pageable pageable);


    // THE SMART RANKING QUERY
    // 1. We search for the keyword in the Name, Description, or Category.
    // 2. ORDER BY CASE: This is the magic.
    //    - if it sku first in rank
    //    - If the Name STARTS with the keyword (Rank 1 - Top result)
    //    - If the Name CONTAINS the keyword (Rank 2)
    //    - If only the Description/Category contains it (Rank 3 - Bottom result)
    @Query("SELECT DISTINCT p, " +
            "(CASE " +
            "WHEN LOWER(v.sku) = LOWER(:keyword) OR LOWER(p.baseSku) = LOWER(:keyword) THEN 1 " +
            "WHEN LOWER(v.sku) LIKE LOWER(CONCAT(:keyword, '%')) OR LOWER(p.baseSku) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2 " +
            "WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 3 " +
            "WHEN LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 4 " +
            "ELSE 5 END) as rank " +
            "FROM Product p " +
            "LEFT JOIN p.variants v " +
            "WHERE LOWER(p.baseSku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR EXISTS (SELECT c FROM p.categories c WHERE LOWER(c.seoSlug) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY rank ASC")
    List<Product> searchProductsWithRanking(@Param("keyword") String keyword);
}