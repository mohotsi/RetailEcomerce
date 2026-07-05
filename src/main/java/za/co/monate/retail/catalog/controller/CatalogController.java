package za.co.monate.retail.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.catalog.dto.BulkProductImportRequest;
import za.co.monate.retail.catalog.dto.ProductImportDto;
import za.co.monate.retail.catalog.dto.ProductResponseDto;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.repository.ProductRepository;
import za.co.monate.retail.catalog.service.CatalogService;
import za.co.monate.retail.catalog.service.SearchService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final CatalogService catalogService;
    private final ProductRepository productRepository;
    private final SearchService searchService;

    @PostMapping(value = "/import/products", consumes = "multipart/form-data")
    public ResponseEntity<String> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            catalogService.processBulkProductsImport(file);
            return ResponseEntity.ok("Product Catalog successfully imported!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    /**
     * =================================================================
     * 1. THE STOREFRONT (READ WITH PAGINATION)
     * =================================================================
     * Real websites don't load 50,000 products at once. They load 20 at a time.
     * Example URL: /api/v1/catalog/products?page=0&size=20&sortBy=name
     */
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDto>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        // Spring Data JPA's findAll(Pageable) automatically runs a SQL COUNT()
        // and a SQL LIMIT/OFFSET query for you!
        Page<ProductResponseDto> productPage = productRepository.findAll(pageable)
                .map(catalogService::mapToResponseDto);
        return ResponseEntity.ok(productPage);
    }

    /**
     * =================================================================
     * METHOD A: MANUAL UI ENTRY (SINGLE PRODUCT)
     * =================================================================
     * Used when a Merchandiser uses the Back Office web form to add
     * a single, highly custom item (like a custom bakery cake).
     */
    @PostMapping("/product")
    public ResponseEntity<Product> createSingleProduct(@RequestBody BulkProductImportRequest request) {
        Product newProduct = catalogService.createCompleteProduct(
                Set.of(request.getCategorySlug()),
                request.getBaseSku(),
                request.getName(),
                request.getDescription(),
                request.getImageUrl()
                // In your service, you'd also pass request.getImageUrl() here!
        );
        return ResponseEntity.ok(newProduct);
    }

    /**
     * =================================================================
     * METHOD B: SYSTEM-TO-SYSTEM EDI (BULK JSON)
     * =================================================================
     * Used by massive suppliers (Coca-Cola, Unilever). Their servers
     * call this endpoint at 2:00 AM automatically. No humans involved.
     */
    @PostMapping("/import/api")
    public ResponseEntity<String> importViaApi(@RequestBody BulkProductImportRequest request) {
        catalogService.createCompleteProduct(
                Set.of(request.getCategorySlug()), request.getBaseSku(),
                request.getName(), request.getDescription(),
                request.getImageUrl()
        );

        for (BulkProductImportRequest.VariantDto v : request.getVariants()) {
            catalogService.addVariantToProduct(
                    request.getBaseSku(), v.getSku(), v.getAttributes(), v.getPrice(), v.getStock()
            );
        }
        return ResponseEntity.ok("Successfully imported via System API.");
    }


    /**
     * =================================================================
     * 1b. CATEGORY NAVIGATION (FILTERED READ WITH PAGINATION)
     * =================================================================
     * Used for the category landing pages (e.g., Pantry, Beverages).
     * Example URL: /api/v1/catalog/category/pantry?page=0&size=20
     */
    @GetMapping("/category/{slug}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // Use the new recursive service method
        Page<ProductResponseDto> productPage = catalogService.getProductsByCategoryRecursive(slug, pageable);

        return ResponseEntity.ok(productPage);
    }

    @PostMapping("/import/products/json")
    public ResponseEntity<String> importProductsJson(@RequestBody List<ProductImportDto> payload) {
        try {
            catalogService.processBulkProductsFromJson(payload);
            return ResponseEntity.ok("JSON Product Catalog successfully imported!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            // @RequestParam tells Spring Boot to look at the end of the URL
            // for "?query=something" and grab that exact word.
            // required = false prevents the server from crashing if the user sends a blank search.
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);



            // 1. Send the raw, messy user input to our Brain (SearchService)
            Page<ProductResponseDto> searchResults = searchService
                    .performSmartSearch(query, pageable);

            // 2. Return the sorted, ranked list to Angular with a 200 OK status
            return ResponseEntity.ok(searchResults);

    }

    @GetMapping("/by-skus")
    public ResponseEntity<List<ProductResponseDto>> getProductsBySkus(@RequestParam List<String> skus) {
        return ResponseEntity.ok(catalogService.getProductsBySkus(skus));
    }


}