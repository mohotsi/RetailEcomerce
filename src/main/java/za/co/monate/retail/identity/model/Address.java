package za.co.monate.retail.identity.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * CLASS: Address
 * PURPOSE: Represents a physical location tied to a user. Supports the concept
 * of an "Address Book" where users can have multiple saved locations.
 * ============================================================================
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link back to our master user table. Both B2B and B2C users need addresses!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private AppUser user;
    @Enumerated(EnumType.STRING)
    private AddressType addressType;
    // --- RECEIVER DETAILS ---
    // Crucial for the delivery driver. Who actually accepts the parcel?
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverPhone;
    private String companyName; // Populated if it's a B2B delivery site
    // --- PHYSICAL LOCATION ---
    private String streetLine1;
    private String streetLine2;
    private String suburb;
    private String city;
    private String province; // e.g., "Western Cape"
    private String postalCode;
    private String countryIso; // e.g., "ZA" (Better than a hardcoded string)
    // --- GEOSPATIAL DATA FOR BOTS & ROUTING ---
    // We use Double instead of String so we don't have to parse it for math calculations.
    private Double latitude;
    private Double longitude;
    // --- ADDRESS BOOK MANAGEMENT ---
    private boolean isDefaultShipping;
    private boolean isDefaultBilling;
    // The "Soft Delete". If false, it doesn't show in the UI/Bot API,
    // but remains in the DB for historical invoice accuracy.
    private boolean isVisible;

    // --- TYPOLOGY ---
    // Instead of two booleans (isBilling, isShipping), an Enum is cleaner.
    public enum AddressType {
        BILLING_ONLY,
        SHIPPING_ONLY,
        BILLING_AND_SHIPPING
    }
}