package za.co.monate.retail.identity.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.catalog.model.PackSize;
import za.co.monate.retail.catalog.model.PricingRule;
import za.co.monate.retail.inventory.model.StockLevel;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String baseSku; 
    private String name;
    
    // Marketing & Display Flags
    private boolean isOnlineOnly;
    private boolean isBestDeal; // Triggers UI badges and Bot priority alerts
    
    // Business Constraints
    private Integer maxPerCustomer; // e.g., Limit 5 per customer (to stop hoarding)
    private boolean agentNegotiable;

    // Pack Sizes (e.g., Single, Case, Pallet)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PackSize> packSizes = new ArrayList<>();

    // Distributed Inventory (Stock across Western Cape, Free State, etc.)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<StockLevel> stockLevels = new ArrayList<>();

    // Active Promotions
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PricingRule> activePricingRules = new ArrayList<>();
    // ========================================================================
    // PURCHASING RESTRICTIONS
    // E.g., "AGE_18" (Liquor) or "SCHEDULE_2" ( meds).
    // The checkout system will block the transaction if the user profile
    // does not meet the requirements.
    // ========================================================================
    private String restrictionCategory; // Null means anyone can buy
    /**
     * Convenience method to get total aggregate stock across the entire country.
     * Useful for initial display before the user inputs their delivery address.
     */
    public int getTotalNationalStock() {
        return stockLevels.stream()
                .mapToInt(StockLevel::getAvailableQuantity)
                .sum();
    }
}