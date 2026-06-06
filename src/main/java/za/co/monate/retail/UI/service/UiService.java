package za.co.monate.retail.UI.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.UI.dto.BannerDto;
import za.co.monate.retail.UI.dto.CreateBannerRequest;
import za.co.monate.retail.catalog.model.PromotionalBanner;
import za.co.monate.retail.catalog.repository.BannerRepository;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UiService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<BannerDto> getActiveHomepageBanners() {
        return bannerRepository.findCurrentlyActiveBanners().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BannerDto mapToDto(PromotionalBanner banner) {
        BannerDto dto = new BannerDto();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImageUrl(banner.getImageUrl());
        dto.setTargetUrl(banner.getTargetUrl());
        return dto;
    }
    @Transactional
    public BannerDto createBanner(CreateBannerRequest request) {
        PromotionalBanner banner = new PromotionalBanner();
        banner.setTitle(request.getTitle());
        banner.setImageUrl(request.getImageUrl());
        banner.setTargetUrl(request.getTargetUrl());

        // Handle optional fields safely
        banner.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        banner.setValidFrom(request.getValidFrom());
        banner.setValidTo(request.getValidTo());
        banner.setActive(true); // Banners are active by default when created

        PromotionalBanner savedBanner = bannerRepository.save(banner);

        return mapToDto(savedBanner); // Reuse your existing mapping method
    }


    // ... your existing methods ...

    /**
     * Parses a CSV file and bulk-inserts promotional banners into the database.
     * Expected CSV Headers: Title, ImageUrl, TargetUrl, SortOrder, ValidFrom, ValidTo
     */
    @Transactional
    public void processBulkBannersImport(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                // Skip the header row
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Split the CSV by commas (Note: this is a basic split. If your URLs contain commas,
                // you would want to use OpenCSV or Apache Commons CSV here).
                String[] data = line.split(",");
                if (data.length < 3) continue; // Skip incomplete rows

                PromotionalBanner banner = new PromotionalBanner();
                banner.setTitle(data[0].trim());
                banner.setImageUrl(data[1].trim());
                banner.setTargetUrl(data[2].trim());

                // Parse Sort Order
                if (data.length > 3 && !data[3].trim().isEmpty()) {
                    banner.setSortOrder(Integer.parseInt(data[3].trim()));
                } else {
                    banner.setSortOrder(0);
                }

                // Parse Dates (Expects ISO format: 2026-06-01T00:00:00)
                if (data.length > 4 && !data[4].trim().isEmpty()) {
                    banner.setValidFrom(LocalDateTime.parse(data[4].trim(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (data.length > 5 && !data[5].trim().isEmpty()) {
                    banner.setValidTo(LocalDateTime.parse(data[5].trim(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                banner.setActive(true);
                bannerRepository.save(banner);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process Banner CSV file: " + e.getMessage());
        }
    }

}