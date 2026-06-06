package za.co.monate.retail.UI.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.UI.dto.BannerDto;
import za.co.monate.retail.UI.dto.CreateBannerRequest;
import za.co.monate.retail.UI.service.UiService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ui")
@RequiredArgsConstructor
public class UiController {

    private final UiService uiService;

    /**
     * Used by: Angular Home.ts
     * Returns only active, time-validated banners sorted by marketing priority.
     */
    @GetMapping("/banners")
    public ResponseEntity<List<BannerDto>> getActiveHomepageBanners() {
        List<BannerDto> banners = uiService.getActiveHomepageBanners();
        return ResponseEntity.ok(banners);
    }

    // Add these imports


    // ... your existing @GetMapping("/banners") method ...

    /**
     * Endpoint: POST /api/v1/ui/banners
     * Used by: Admin Backoffice
     * Action: Creates a new promotional banner. Restricted to System Admins.
     */
    @PostMapping("/banners")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<BannerDto> createBanner(@RequestBody CreateBannerRequest request) {
        BannerDto newBanner = uiService.createBanner(request);
        return ResponseEntity.ok(newBanner);
    }





    /**
     * Endpoint: POST /api/v1/ui/banners/import
     * Used by: Admin Backoffice
     * Action: Bulk uploads promotional banners via a CSV file.
     */
    @PostMapping(value = "/banners/import", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<String> importBannersCsv(@RequestParam("file") MultipartFile file) {
        try {
            uiService.processBulkBannersImport(file);
            return ResponseEntity.ok("Promotional banners successfully imported!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }


}