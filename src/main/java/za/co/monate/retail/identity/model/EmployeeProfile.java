package za.co.monate.retail.identity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * CLASS: EmployeeProfile
 * PURPOSE: Holds internal HR/Staff data for Merchandisers, Warehouse Workers,
 * and Customer Support agents.
 * ============================================================================
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfile {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String employeeNumber; // e.g., "EMP-9932"

    private String department; // e.g., "Customer Success", "Logistics"

    // --- SUPPORT METRICS ---
    // How many carts has this employee successfully checked out for customers?
    private int assistedSalesCount;

    // Customer Support agents are usually given a daily budget to give away 
    // free shipping or 10% discounts to keep angry customers happy.
    private double dailyDiscretionaryBudget;
}