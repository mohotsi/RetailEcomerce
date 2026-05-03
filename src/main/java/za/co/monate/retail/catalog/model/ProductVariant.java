package za.co.monate.retail.catalog.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the actual Barcode/SKU printed on the physical item
    @Column(nullable = false, unique = true)
    private String sku; // e.g., "NIKE-TEE-001-RED-M"

    // What makes this variant unique? (e.g., "Red - Medium", "500ml", "1kg")
    private String attributeSummary; 

    // Price is stored as BigDecimal, NEVER as a double/float to prevent rounding errors!
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;
    @Column(length = 500)
    private String imageUrl;
    // Links back to the Base Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}