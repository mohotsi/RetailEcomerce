package za.co.monate.retail.identity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import za.co.monate.retail.identity.model.enums.AccountStatus;
import za.co.monate.retail.identity.model.enums.AppRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_users") // 'user' is often a reserved keyword in databases like Postgres
public class AppUser implements UserDetails {

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

    // ========================================================================
    // ADDRESS BOOK
    // A user can have multiple addresses (Home, Office, Holiday House).
    // The cascade ensures if a user is deleted, their addresses are deleted.
    // ========================================================================
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addressBook = new ArrayList<>();

    // ========================================================================
    // SPRING SECURITY REQUIRED METHODS
    // These methods translate our custom database logic into a language
    // that the Spring Security framework understands.
    // ========================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // We take our AppRole (e.g., MERCHANDISER) and turn it into an Authority.
        // Spring Security expects roles to start with "ROLE_", so we add it.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email; // In our system, the Email is the Username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Here we use our custom AccountStatus!
        return status != AccountStatus.PERMANENTLY_BANNED
                && status != AccountStatus.TEMPORARILY_BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // B2B clients waiting for Admin approval cannot log in yet
        return status != AccountStatus.PENDING_APPROVAL;
    }
}