package za.co.monate.retail.checkout.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * ============================================================================
 * CLASS: FulfillmentProposal
 * PURPOSE: The master response object. This is what the OrderRoutingService
 * returns when an AI Bot asks "Can I get 500 cases of Aromat?"
 * ============================================================================
 */
@Data
@Builder
public class FulfillmentProposal {
    // The specific product identifier the bot asked for
    private String sku;
    
    // What the bot originally asked for
    private int requestedQuantity;
    
    // A quick boolean flag. If false, the bot knows it must find another supplier.
    private boolean canFulfillEntirely;
    
    // The absolute bottom-line cost (Product Price * Qty + ALL delivery fees)
    private double totalFulfillmentCost; 
    
    // If the order is split across 2 provinces, this list will have 2 DeliveryRoute objects in it.
    private List<DeliveryRoute> routes;  
}