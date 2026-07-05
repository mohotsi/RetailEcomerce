package za.co.monate.retail.catalog.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * =======================================================================================
 * PROMOTIONAL BANNER ENTITY
 * Drives the homepage carousel. Decoupled from the Product table entirely; it simply
 * acts as a visual router to send users to specific categories or search URLs.
 * =======================================================================================
 */
@Entity
@Table(name = "promotional_banners")
@Data
public class PromotionalBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Alt-text for accessibility and internal tracking
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl; // S3 link or local asset path (e.g., /assets/thokoman.jpg)

    @Column(name = "target_url", nullable = false,length = 1000)
    private String targetUrl; // The frontend route to trigger (e.g., /category/thokoman-brand)
    
    // Master kill switch for marketing to disable an ad instantly
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Determines the sequence of the carousel slides (1, 2, 3)
    @Column(name = "sort_order")
    private Integer sortOrder;

    /*
     * --- TIME-BOXING ---
     * Banners disappear automatically when the promotion ends, ensuring the UI 
     * never displays a banner leading to an expired category.
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;
}