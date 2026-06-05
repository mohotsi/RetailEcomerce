package za.co.monate.retail.catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "Spices"
    private String seoSlug; // e.g., "/pantry/seasonings/spices"
    private String description;
    private int displayOrder;

    // The existing link to the parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("subCategories") // Prevents infinite recursion
    private Category parentCategory;

    // --- NEW: The link to the children ---
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> subCategories;


    /* * MERCHANDISING FLAGS
     * showInNav = true -> Shows in the red dropdown menu
     * showInNav = false -> Hidden from menu, used ONLY for banner promotions
     */
    @Column(name = "show_in_nav", nullable = false)
    private boolean showInNav = true;

    /*
     * TIME-BOXING (Automated Expiration)
     * Marketing can set these so specials automatically disappear at midnight.
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;


}