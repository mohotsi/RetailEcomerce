package za.co.monate.retail.identity.model.enums;

/**
 * ============================================================================
 * ENUM: AccountStatus
 * PURPOSE: The different "Blocked" levels you requested.
 * <p>
 * TEACHING MOMENT:
 * We never DELETE a user from a database in an Enterprise system (it breaks
 * past order history). Instead, we change their status. This is called a "Soft Delete"
 * or "State Machine" approach.
 * ============================================================================
 */
public enum AccountStatus {
    PENDING_APPROVAL,      // Used for B2B: Registered but waiting for Admin to verify VAT
    ACTIVE,                // Normal purchasing state
    RESTRICTED_PURCHASING, // Can log in and view history, but checkout is blocked (e.g., unpaid bills)
    TEMPORARILY_BLOCKED,   // Blocked due to suspicious activity (e.g., Bot spam)
    PERMANENTLY_BANNED     // Blacklisted
}