package za.co.monate.retail.checkout.service;

import org.springframework.stereotype.Service;
import za.co.monate.retail.identity.model.AppUser;
import za.co.monate.retail.identity.model.EmployeeProfile;
import za.co.monate.retail.identity.model.enums.AppRole;
// Assume we have an Order and Cart class built
// import za.co.monate.retail.model.Order;
// import za.co.monate.retail.model.Cart;

/**
 * ============================================================================
 * CLASS: AssistedCheckoutService
 * PURPOSE: Allows a CUSTOMER_SUPPORT agent to finalize an order for a customer.
 * ============================================================================
 */
@Service
public class AssistedCheckoutService {

    /**
     * Completes an order on behalf of a customer.
     *
     * @param supportAgent         The employee helping the customer
     * @param customer             The stuck B2C or B2B customer
     * @param applyApologyDiscount If true, the agent uses their budget to give a discount
     */
    public void checkoutOnBehalfOf(AppUser supportAgent, AppUser customer, boolean applyApologyDiscount) {

        // 1. SECURITY CHECK: Ensure the person calling this is actually Support!
        if (supportAgent.getRole() != AppRole.CUSTOMER_SUPPORT) {
            throw new SecurityException("Only Customer Support can assist with checkouts!");
        }

        EmployeeProfile agentProfile = supportAgent.getEmployeeProfile();

        // 2. FETCH THE CUSTOMER'S ABANDONED CART
        // Cart customerCart = cartRepository.findByUserId(customer.getId());
        System.out.println("Support Agent " + agentProfile.getFirstName() +
                " has accessed the cart for " + customer.getEmail());

        // 3. APPLY DISCRETIONARY DISCOUNT (If authorized and requested)
        if (applyApologyDiscount) {
            if (agentProfile.getDailyDiscretionaryBudget() >= 50.0) {
                System.out.println("Applying R50 Apology Discount to save the sale...");
                // Deduct from the agent's daily allowance so they don't abuse it
                agentProfile.setDailyDiscretionaryBudget(agentProfile.getDailyDiscretionaryBudget() - 50.0);
            } else {
                System.out.println("Agent has run out of discount budget for today.");
            }
        }

        // 4. FINALIZE THE ORDER
        // Order finalizedOrder = orderRoutingService.generateOrder(customerCart);
        System.out.println("Order successfully finalized on behalf of the customer!");

        // 5. UPDATE AGENT METRICS
        // Give the agent credit for rescuing the sale!
        agentProfile.setAssistedSalesCount(agentProfile.getAssistedSalesCount() + 1);

        // TODO: Save updated Agent Profile and new Order to the database
    }
}