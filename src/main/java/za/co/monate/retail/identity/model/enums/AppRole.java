package za.co.monate.retail.identity.model.enums;

public enum AppRole {
    B2C_CUSTOMER,
    B2B_CUSTOMER,
    MACHINE_AGENT,
    MERCHANDISER,
    WAREHOUSE_WORKER,

    // ========================================================================
    // NEW ROLE: CUSTOMER_SUPPORT
    // Can view user carts, apply discretionary discounts, and finalize
    // checkouts on behalf of stuck customers.
    // ========================================================================
    CUSTOMER_SUPPORT,

    SYSTEM_ADMIN
}