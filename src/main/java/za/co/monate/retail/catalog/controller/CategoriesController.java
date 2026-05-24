package za.co.monate.retail.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.catalog.CategoryImportRow;
import za.co.monate.retail.catalog.dto.CategoryNodeDto;
import za.co.monate.retail.catalog.dto.CategorySummaryDto;
import za.co.monate.retail.catalog.repository.CategoryRepository;
import za.co.monate.retail.catalog.repository.ProductRepository;
import za.co.monate.retail.catalog.service.CatalogService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoriesController {

    private final CatalogService catalogService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<String> importCategories(@RequestParam("file") MultipartFile file) {
        // Logic to parse CSV and map to CategoryImportRow
        catalogService.processBulkCategoriesImport(file);
        return ResponseEntity.ok("Taxonomy updated successfully");
    }

    @PostMapping(value = "/import/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<String> importCategoriesJson(@RequestBody List<CategoryImportRow> categories) {
        catalogService.processBulkCategoriesFromJson(categories);
        return ResponseEntity.ok("Taxonomy updated successfully from JSON payload");
    }

    /**
     * =================================================================
     * NAVIGATION MENU ENDPOINT
     * =================================================================
     * Returns a nested tree structure of all categories.
     */
    @GetMapping("/navigation")
    public ResponseEntity<List<CategorySummaryDto>> getNavigationMenu() {
        // We only fetch the roots. Hibernate/JPA will automatically
        // populate the 'subCategories' list for each root.
        List<CategorySummaryDto> tree = categoryRepository.findAllByParentCategoryIsNull().stream()
                .map(category ->
                        {
                            val categorySummaryDto = new CategorySummaryDto();
                            categorySummaryDto.setId(category.getId());
                            categorySummaryDto.setName(category.getName());
                            categorySummaryDto.setSeoSlug(category.getSeoSlug());
                            return categorySummaryDto;


                        }

                ).collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/navigationTree")
    public ResponseEntity<List<CategoryNodeDto>> getNavigationTree() {
        return ResponseEntity.ok(catalogService.getNavigationTree());
    }
}