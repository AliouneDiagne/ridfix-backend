package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.CommonDTOs;
import it.ridfix.backend.dto.ProductDTOs;
import it.ridfix.backend.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public CommonDTOs.PageResponse<ProductDTOs.ProductResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return products.list(q, categoryId, brandId, minPrice, maxPrice, inStock, activeOnly, page, size, sort);
    }

    @GetMapping("/{id}")
    public ProductDTOs.ProductResponse get(@PathVariable UUID id) {
        return products.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductDTOs.ProductResponse create(@Valid @RequestBody ProductDTOs.ProductCreateRequest req) {
        return products.create(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProductDTOs.ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductDTOs.ProductUpdateRequest req) {
        return products.update(id, req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDTOs.ProductResponse uploadImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return products.uploadImage(id, file);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        products.delete(id);
    }
}
