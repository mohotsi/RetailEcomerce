package za.co.monate.retail.inventory.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.identity.model.Product;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private int availableQuantity;
    
    // Bots reserve stock during negotiation to prevent race conditions
    private int reservedQuantity; 
}