package za.co.monate.retail.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.inventory.model.enums.WarehouseType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "Gauteng Main Distribution Centre"
    private String province; // "Western Cape", "Pretoria", "Eastern Cape", "Free State"
    
    // Address fields
    private String streetAddress;
    private String city;
    private String postalCode;

    // Crucial for the 200km delivery fee calculation later
    private Double latitude;
    private Double longitude;
    // Inside our Warehouse.java
    @Enumerated(EnumType.STRING)
    private WarehouseType facilityType;

    // If this is a Medirite inside a Checkers, this points to the Checkers ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_facility_id")
    private Warehouse parentFacility;
}