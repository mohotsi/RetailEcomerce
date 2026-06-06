package za.co.monate.retail.UI.dto;

import lombok.Data;

/**
 * =======================================================================================
 * BANNER DTO
 * Exposes only the visual routing data required by the Angular carousel.
 * Keeps backend database fields (like active status and timestamps) hidden from the frontend.
 * =======================================================================================
 */
@Data
public class BannerDto {
    private Long id;
    private String title;
    private String imageUrl;
    private String targetUrl;
}