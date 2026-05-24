package za.co.monate.retail.catalog.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cdnUrl; // e.g., "https://cdn.monate.co.za/images/aromat.jpg"
    private String altText; // Crucial for visually impaired users and SEO

    private boolean isPrimary; // The main image shown on the search results

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;
}