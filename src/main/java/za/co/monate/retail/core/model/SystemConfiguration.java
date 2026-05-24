package za.co.monate.retail.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class SystemConfiguration {
    @Id
    private String configKey; // e.g., "DELIVERY_FEE_RULES"

    private double freeDeliveryRadiusKm; // Admin sets to 200.0
    private double outOfBoundsFlatFee;   // Admin sets to 1000.0
}