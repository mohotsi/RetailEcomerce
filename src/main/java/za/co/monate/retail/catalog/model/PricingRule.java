package za.co.monate.retail.catalog.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.catalog.model.enums.PromoType;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PromoType promoType;

    private String description; // E.g., "Winter Buy 3 Get 1 Free"

    // Generic condition fields used differently based on PromoType
    private Integer conditionQuantity; // The 'X' in Buy X
    private Integer rewardQuantity;    // The 'Y' in Get Y Free
    private Double discountValue;      // Used for % or Flat amount
    
    // Tiered pricing specific
    private Integer tierMinQuantity;
    private Integer tierMaxQuantity;
    private Double tierPrice;

    // Time-bound rules (Crucial for bots parsing flash sales)
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;
    // ========================================================================
    // THE LOYALTY CARD CHECK
    // If true, the OrderRoutingService will check if the user's B2CProfile
    // has a valid loyaltyCardNumber before applying this discount.
    // ========================================================================
    private boolean requiresLoyaltyCard;
    // Link back to the product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference // Prevents infinite recursion when converting to JSON
    private Product product;
}