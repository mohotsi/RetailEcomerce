package za.co.monate.retail.core.util;

/**
 * ============================================================================
 * CLASS: DistanceCalculator
 * PURPOSE: The "Math Engine" for calculating how far a customer is from our warehouse.
 * 
 * WHY THIS MATTERS FOR MACHINE CUSTOMERS:
 * AI Bots don't care about "Free Delivery" banners. They care about exact 
 * mathematical costs to calculate their ROI. Instead of paying Google Maps 
 * thousands of Rands for their API, we use pure math (The Haversine Formula) 
 * to calculate the distance between two GPS coordinates on a sphere (the Earth).
 * ============================================================================
 */
public class DistanceCalculator {

    // The Earth is not flat! The approximate radius of the Earth in kilometers.
    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculates the "as the crow flies" distance between two points.
     * 
     * @param startLat The latitude of the Warehouse
     * @param startLong The longitude of the Warehouse
     * @param endLat The latitude of the Customer/Bot
     * @param endLong The longitude of the Customer/Bot
     * @return The distance in kilometers
     */
    public static double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        
        // Math.toRadians converts degrees (like 33.9249) into radians, 
        // which is required for trigonometric math in Java.
        double dLat = Math.toRadians(endLat - startLat);
        double dLon = Math.toRadians(endLong - startLong);

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        // This is the core of the Haversine formula.
        // It calculates the 'great-circle' distance between two points on a sphere.
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) * Math.cos(startLat) * Math.cos(endLat);
        
        // 'c' represents the angular distance in radians.
        double c = 2 * Math.asin(Math.sqrt(a));
        
        // Multiply by the Earth's radius to get the final distance in Kilometers.
        return EARTH_RADIUS_KM * c;
    }
}