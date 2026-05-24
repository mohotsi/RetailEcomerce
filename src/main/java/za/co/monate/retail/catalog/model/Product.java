package za.co.monate.retail.catalog.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.identity.model.enums.AppRole;
import za.co.monate.retail.inventory.model.StockLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ============================================================================
 * CLASS: Product
 * PURPOSE: The central entity of the Catalog domain. It holds metadata,
 * search optimization, and security constraints.
 * <p>
 * NOTE: The actual Price is stored in the 'PackSize' entity, because a single
 * Product (e.g., Aromat) can be sold in multiple sizes (Single vs Case).
 * ============================================================================
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products", indexes = {
        // We explicitly define indexes here.
        // Ensure the 'columnList' matches your actual field names!
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_base_sku", columnList = "baseSku")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The universal identifier for bots and human inventory scanning
    @Column(unique = true, nullable = false)
    private String baseSku;

    private String name;

    @Column(length = 1000) // Descriptions can be long
    private String description;

    // Crucial for Human users finding the product on Google
    private String seoSlug; // e.g., "aromat-original-seasoning-200g"

    // --- BRAND & SEARCH (For B2B Procurement Rules & AI Trust Filters) ---
    private String manufacturer; // e.g., "Unilever"
    private String brand;        // e.g., "Knorr"

    @Column(length = 500)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_search_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> searchTags = new ArrayList<>(); // e.g., ["seasoning", "spice"]

    // Used by AI Bots to ensure they don't buy low-quality alternatives
    private Double averageRating;

    // --- ROLE-BASED ACCESS CONTROL (Replaces legacy booleans) ---
    // Controls who is legally allowed to see or buy this item.
    // e.g., Set to B2B_CUSTOMER to hide wholesale items from standard retail shoppers.
    @Enumerated(EnumType.STRING)
    private AppRole minimumRoleToView;

    @Enumerated(EnumType.STRING)
    private AppRole minimumRoleToBuy;

    // --- BUSINESS & FULFILLMENT CONSTRAINTS ---
    private Integer maxPerCustomer; // Stops hoarding (e.g., limit 5 per customer)
    private boolean agentNegotiable; // Can an AI Bot counter-offer the price?
    private double absoluteFloorPrice; // The lowest price the bot engine will accept

    // Multi-Channel Fulfillment Flags
    private boolean eligibleForDelivery;
    private boolean eligibleForClickAndCollect;
    private boolean isDisplayOnly; // Cannot be bought online (e.g., massive in-store only appliances)

    // --- TAXONOMY (CATEGORIES) ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_category_link",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    // --- RICH MEDIA ---
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductImage> images = new ArrayList<>();

    // --- PACK SIZES (Where the Price lives) ---
    // e.g., Single Shaker, Shrink of 6, Case of 24
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PackSize> packSizes = new ArrayList<>();

    // --- DISTRIBUTED INVENTORY ---
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<StockLevel> stockLevels = new ArrayList<>();

    // --- ACTIVE PROMOTIONS ---
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PricingRule> activePricingRules = new ArrayList<>();

    // A Base Product has many Variants (sizes/colors)
    // CascadeType.ALL means if we delete the Base Product, it deletes all variants
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    /**
     * Convenience method to get total aggregate stock across all warehouses.
     * Useful for the initial UI display before routing logic takes over.
     */
    public int getTotalNationalStock() {
        if (stockLevels == null || stockLevels.isEmpty()) {
            return 0;
        }
        return stockLevels.stream()
                .mapToInt(StockLevel::getAvailableQuantity)
                .sum();
    }
}