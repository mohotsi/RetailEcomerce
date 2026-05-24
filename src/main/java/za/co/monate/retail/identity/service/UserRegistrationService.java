package za.co.monate.retail.identity.service;

import org.springframework.stereotype.Service;
import za.co.monate.retail.identity.model.AppUser;
import za.co.monate.retail.identity.model.B2BProfile;
import za.co.monate.retail.identity.model.B2CProfile;
import za.co.monate.retail.identity.model.enums.AccountStatus;
import za.co.monate.retail.identity.model.enums.AppRole;

/**
 * ============================================================================
 * CLASS: UserRegistrationService
 * PURPOSE: Handles the different onboarding flows for different account types.
 * ============================================================================
 */
@Service
public class UserRegistrationService {

    // We will inject a real EmailService here later to talk to MailHog
    // private final EmailService emailService;

    /**
     * Registers a standard B2C Customer.
     * LOGIC: Instantly active. Sends a welcome email.
     */
    public AppUser registerB2CCustomer(String email, String rawPassword, B2CProfile profileData) {

        AppUser newUser = AppUser.builder()
                .email(email)
                .passwordHash(rawPassword) // (We will encrypt this when we add Security)
                .role(AppRole.B2C_CUSTOMER)
                .status(AccountStatus.ACTIVE) // Instantly Active!
                .build();

        profileData.setUser(newUser);
        newUser.setB2cProfile(profileData);

        // TODO: Save to Database via UserRepository

        // Mock Email Notification
        System.out.println("EMAIL SENT TO " + email + ": Welcome to Monate Retail! Use your Xtra Savings card today.");

        return newUser;
    }

    /**
     * Registers a B2B Corporate Client.
     * LOGIC: Status is PENDING. Requires human admin review.
     */
    public AppUser registerB2BClient(String email, String rawPassword, B2BProfile companyData) {

        AppUser newUser = AppUser.builder()
                .email(email)
                .passwordHash(rawPassword)
                .role(AppRole.B2B_CUSTOMER)
                .status(AccountStatus.PENDING_APPROVAL) // Blocked until Admin reviews VAT number!
                .build();

        companyData.setUser(newUser);
        newUser.setB2bProfile(companyData);

        // TODO: Save to Database via UserRepository

        // Mock Email Notification
        System.out.println("EMAIL SENT TO " + email + ": We have received your B2B application. " +
                "Our admin team is verifying your VAT number (" + companyData.getVatNumber() + "). " +
                "You will be notified once your corporate account is active.");

        return newUser;
    }
}