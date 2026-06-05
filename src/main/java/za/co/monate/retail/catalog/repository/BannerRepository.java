package za.co.monate.retail.catalog.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.co.monate.retail.catalog.model.PromotionalBanner;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<PromotionalBanner, Long> {
    
    /*
     * Retrieves active banners for the homepage carousel.
     * Enforces active switch, time-boxing, and correct marketing sort order.
     */
    @Query("SELECT b FROM PromotionalBanner b WHERE b.active = true " +
           "AND (b.validFrom IS NULL OR b.validFrom <= CURRENT_TIMESTAMP) " +
           "AND (b.validTo IS NULL OR b.validTo >= CURRENT_TIMESTAMP) " +
           "ORDER BY b.sortOrder ASC")
    List<PromotionalBanner> findCurrentlyActiveBanners();
}