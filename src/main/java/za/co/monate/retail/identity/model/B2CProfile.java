package za.co.monate.retail.identity.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * ============================================================================
 * CLASS: B2CProfile
 * PURPOSE: Holds data specific to standard retail shoppers.
 * ============================================================================
 */
@Entity
@Data
public class B2CProfile {
    @Id
    private Long id;

    @OneToOne
    @MapsId // Shares the same ID as the AppUser table
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String firstName;
    private String lastName;

    // The "Xtra Savings" concept. Used to unlock specific PricingRules.
    private String loyaltyCardNumber; 

    // Used to verify if they can buy restricted products (e.g., alcohol, meds)
    private LocalDate dateOfBirth; 
}