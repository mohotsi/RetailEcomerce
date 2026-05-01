package za.co.monate.retail.identity.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * ============================================================================
 * CLASS: B2BProfile
 * PURPOSE: Holds corporate data. This allows humans or AI bots to buy for a company.
 * ============================================================================
 */
@Entity
@Data
public class B2BProfile {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String companyName;
    private String registrationNumber; // e.g., CIPC number in SA
    private String vatNumber;

    // --- BUYING ON ACCOUNT LOGIC ---
    // Instead of paying via credit card, businesses are given a credit facility.
    private boolean isApprovedForCredit;
    private double creditLimit;
    private double availableCreditBalance; // Decreases as they order, resets when they pay their invoice
}