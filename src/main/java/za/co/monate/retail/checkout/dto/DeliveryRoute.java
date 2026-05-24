package za.co.monate.retail.checkout.dto;

import lombok.Builder;
import lombok.Data;

/**
 * ============================================================================
 * CLASS: DeliveryRoute
 * PURPOSE: A Data Transfer Object (DTO). This is the exact JSON package we
 * send out. It represents a SINGLE leg of a delivery.
 * <p>
 * TEACHING MOMENT: What is a DTO?
 * We don't want to send our raw Database Entities (like Warehouse.java)
 * over the internet. That exposes too much backend info. Instead, we create
 * DTOs—custom objects built specifically to carry data to the frontend or the Bot.
 * ============================================================================
 */
@Data
@Builder
public class DeliveryRoute {
    // The name of the warehouse doing the sending (e.g., "Western Cape")
    private String fromWarehouse;

    // How many units of the product are coming from this specific warehouse
    private int quantityAllocated;

    // The Haversine distance calculated earlier
    private double distanceKm;

    // The exact cost. Bots read this to decide if the deal is worth it.
    private double deliveryFee;

    // SLA (Service Level Agreement). Bots use this to update their inventory prediction.
    private int estimatedDaysToDeliver;
}