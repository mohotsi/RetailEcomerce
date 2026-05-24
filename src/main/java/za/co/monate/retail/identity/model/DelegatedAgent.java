package za.co.monate.retail.identity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * CLASS: DelegatedAgent
 * PURPOSE: Allows a human B2C (or B2B) user to give an AI Bot permission to
 * shop on their behalf, but with strict safety rails.
 * <p>
 * TEACHING MOMENT:
 * Never give a bot your main password! Instead, the user generates a specific
 * API Key for the bot. If the bot misbehaves, the user just clicks "Revoke"
 * to delete the key, and their main account remains totally safe.
 * ============================================================================
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelegatedAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The human user who "owns" this bot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private AppUser owner;

    private String agentName; // e.g., "Thapelo's Smart Fridge", "My Custom Python Scraper"

    // The cryptographic token the bot will use to log in (like a password just for the bot)
    @Column(unique = true, nullable = false)
    private String agentApiKey;

    // --- BOT SAFETY RAILS ---

    // Bots can only spend this much per month without human approval
    private double monthlySpendingLimit;

    // Tracks how much the bot has spent this month
    private double currentMonthSpend;

    // If true, the bot can negotiate and buy. If false, the human revoked access.
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}