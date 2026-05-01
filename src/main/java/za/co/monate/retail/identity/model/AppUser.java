package za.co.monate.retail.identity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.identity.model.enums.AccountStatus;
import za.co.monate.retail.identity.model.enums.AppRole;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_users") // 'user' is often a reserved keyword in databases like Postgres
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash; // We will use BCrypt later to encrypt this

    @Enumerated(EnumType.STRING)
    private AppRole role;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    // We link the profiles here. Only ONE of these will be populated depending on the Role.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private B2CProfile b2cProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private B2BProfile b2bProfile;
    // ========================================================================
    // DELEGATED AI AGENTS
    // A single human user might have multiple bots working for them.
    // E.g., A Smart Fridge bot for groceries, and a Browser bot for electronics.
    // ========================================================================
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DelegatedAgent> authorizedBots = new ArrayList<>();
    // ========================================================================
    // INTERNAL STAFF PROFILE
    // Populated ONLY if the user is a CUSTOMER_SUPPORT, MERCHANDISER, etc.
    // ========================================================================
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private EmployeeProfile employeeProfile;
}