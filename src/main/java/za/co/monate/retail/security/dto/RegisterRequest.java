package za.co.monate.retail.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.monate.retail.identity.model.enums.AppRole;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    // For this tutorial, we will let them choose a role. 
    // In production, you might default this to B2C_CUSTOMER.
    private AppRole role;
}