package za.co.monate.retail.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.monate.retail.catalog.model.Product;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // We look up base products by their Base SKU, not their database ID
    Optional<Product> findByBaseSku(String baseSku);
}