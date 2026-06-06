package za.co.monate.retail.UI.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateBannerRequest {
    private String title;
    private String imageUrl;
    private String targetUrl;
    private Integer sortOrder;
    
    // Time-boxing fields
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}