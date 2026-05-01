package za.co.monate.retail.catalog.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

// Inside za.co.monate.retail.catalog.model (or a shared 'core' package)
@Embeddable // This means it doesn't get its own table, it becomes columns in the Product/PackSize table
@Data
public class Price {
    private Double amount;
    private String currencyIso; // e.g., "ZAR", "USD"
    private String formattedValue; // e.g., "R 120.00" - Great for UI and Bots scraping visual text
}


