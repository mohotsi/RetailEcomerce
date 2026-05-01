package za.co.monate.retail.checkout.service;

import org.springframework.stereotype.Service;
import za.co.monate.retail.checkout.dto.DeliveryRoute;
import za.co.monate.retail.checkout.dto.FulfillmentProposal;
import za.co.monate.retail.catalog.model.PackSize;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.inventory.model.StockLevel;
import za.co.monate.retail.core.util.DistanceCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * CLASS: OrderRoutingService
 * PURPOSE: The Brain of the Distributed Order Management System (DOMS).
 * ============================================================================
 */
@Service
public class OrderRoutingService {

    private static final double FREE_DELIVERY_RADIUS_KM = 200.0;
    private static final double OUT_OF_BOUNDS_FEE = 1000.0;

    /**
     * @param packSize The specific variation the Bot wants (e.g., "Case of 12 Aromat")
     * @param requestedQty How many of this pack size the bot wants
     * @param customerLat The Bot/Customer's Latitude
     * @param customerLong The Bot/Customer's Longitude
     */
    public FulfillmentProposal generateProposal(PackSize packSize, int requestedQty, double customerLat, double customerLong) {
        
        // 1. Get the parent product from the pack size so we can check its stock levels
        Product product = packSize.getProduct();
        
        List<DeliveryRoute> routes = new ArrayList<>();
        int remainingToFulfill = requestedQty;
        double totalDeliveryFees = 0.0;

        /*
         * STEP 1: SORT WAREHOUSES BY DISTANCE
         */
        List<StockLevel> sortedStock = product.getStockLevels().stream()
                .filter(stock -> stock.getAvailableQuantity() > 0)
                .sorted(Comparator.comparingDouble(stock -> 
                        DistanceCalculator.calculateDistance(
                                customerLat, customerLong, 
                                stock.getWarehouse().getLatitude(), 
                                stock.getWarehouse().getLongitude()
                        )
                ))
                .collect(Collectors.toList());

        /*
         * STEP 2: CONSUME STOCK
         */
        for (StockLevel stock : sortedStock) {
            if (remainingToFulfill <= 0) break;

            double distance = DistanceCalculator.calculateDistance(
                    customerLat, customerLong, 
                    stock.getWarehouse().getLatitude(), 
                    stock.getWarehouse().getLongitude()
            );

            int qtyToTake = Math.min(stock.getAvailableQuantity(), remainingToFulfill);
            double deliveryFee = (distance <= FREE_DELIVERY_RADIUS_KM) ? 0.0 : OUT_OF_BOUNDS_FEE;
            
            routes.add(DeliveryRoute.builder()
                    .fromWarehouse(stock.getWarehouse().getName())
                    .quantityAllocated(qtyToTake)
                    .distanceKm(Math.round(distance * 100.0) / 100.0)
                    .deliveryFee(deliveryFee)
                    .estimatedDaysToDeliver(distance <= FREE_DELIVERY_RADIUS_KM ? 1 : 4)
                    .build());

            totalDeliveryFees += deliveryFee;
            remainingToFulfill -= qtyToTake;
        }

        /*
         * ====================================================================
         * STEP 3: PACKAGE THE FINAL RESPONSE (THE FIX)
         * ====================================================================
         * We now get the price directly from the PackSize object!
         * We use packSize.getPrice() instead of looking at the Product.
         * We also return the packSize.getBarcode() so the bot knows exactly 
         * which item variation this quote is for.
         * ====================================================================
         */
        double costOfGoods = packSize.getPrice() * requestedQty;
        double finalTotalCost = costOfGoods + totalDeliveryFees;

        return FulfillmentProposal.builder()
                .sku(packSize.getBarcode()) // Changed to use the PackSize barcode
                .requestedQuantity(requestedQty)
                .canFulfillEntirely(remainingToFulfill == 0)
                .totalFulfillmentCost(finalTotalCost) 
                .routes(routes)
                .build();
    }
}