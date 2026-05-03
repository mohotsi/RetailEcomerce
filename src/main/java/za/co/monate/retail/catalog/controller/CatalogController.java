package za.co.monate.retail.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.co.monate.retail.catalog.dto.BulkProductImportRequest;
import za.co.monate.retail.catalog.model.Product;
import za.co.monate.retail.catalog.repository.ProductRepository;
import za.co.monate.retail.catalog.service.CatalogService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final CatalogService catalogService;
    private final ProductRepository productRepository;

    /**
     * =================================================================
     * 1. THE STOREFRONT (READ WITH PAGINATION)
     * =================================================================
     * Real websites don't load 50,000 products at once. They load 20 at a time.
     * Example URL: /api/v1/catalog/products?page=0&size=20&sortBy=name
     */
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        // Spring Data JPA's findAll(Pageable) automatically runs a SQL COUNT() 
        // and a SQL LIMIT/OFFSET query for you!
        Page<Product> productPage = productRepository.findAll(pageable);
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
                request.getDescription()
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
                Set.of(request.getCategorySlug()), request.getBaseSku(), request.getName(), request.getDescription()
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
     * METHOD C: HUMAN BULK UPLOAD (CSV EXCEL FILE)
     * =================================================================
     * Notice we now expect the 6th column to be the AWS Image URL!
     */
    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importViaCsv(@RequestParam("file") MultipartFile file) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; } // Skip header
                
                // Format: BaseSku,VariantSku,Attributes,Price,Stock,ImageURL
                String[] data = line.split(",");
                if (data.length >= 6) {
                    // Update your service method to accept this 6th parameter: data[5].trim()
                    catalogService.addVariantToProduct(
                            data[0].trim(), data[1].trim(), data[2].trim(), 
                            new BigDecimal(data[3].trim()), Integer.parseInt(data[4].trim())
                    );
                    count++;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse CSV", e);
            return ResponseEntity.internalServerError().body("Failed to process CSV file.");
        }

        return ResponseEntity.ok("Successfully processed CSV. Added " + count + " variants.");
    }
}