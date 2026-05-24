package za.co.monate.retail.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.monate.retail.catalog.model.ProductVariant;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    // This is the most executed query in your entire system. 
    // Every time a scanner beeps at checkout, this method runs.
    Optional<ProductVariant> findBySku(String sku);
}