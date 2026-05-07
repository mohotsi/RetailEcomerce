package za.co.monate.retail.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.monate.retail.catalog.model.Product;

import java.util.Collection;
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
}