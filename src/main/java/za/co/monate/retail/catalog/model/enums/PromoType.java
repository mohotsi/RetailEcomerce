package za.co.monate.retail.catalog.model.enums;

public enum PromoType {
    FIXED_PERCENTAGE,      // E.g., 10% off
    FIXED_AMOUNT,          // E.g., R50 off the total
    BUY_X_GET_Y_FREE,      // E.g., Buy 3, Get 1
    TIERED_VOLUME,         // E.g., 1-99 = R10, 100+ = R8.50
    BUNDLE_DEAL,           // E.g., Buy Product A + Product B for a set price
    BONUS_BUY              // E.g., Buy 1000 units, get a R500 store credit
}