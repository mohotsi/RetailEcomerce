package za.co.monate.retail.inventory.model.enums;

public enum StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK,
    PRE_ORDER,
    DISCONTINUED,
    REPLENISHING // Expected on the next delivery truck
}