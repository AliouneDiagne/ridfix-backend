package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.CatalogDTOs;
import it.ridfix.backend.services.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    // =========================
    // PUBLIC
    // =========================

    @GetMapping("/categories")
    public List<CatalogDTOs.CategoryDTO> categories() {
        return catalog.listCategories();
    }

    @GetMapping("/brands")
    public List<CatalogDTOs.BrandDTO> brands() {
        return catalog.listBrands();
    }

    @GetMapping("/categories/{id}")
    public CatalogDTOs.CategoryDTO getCategory(@PathVariable UUID id) {
        return catalog.getCategory(id);
    }

    @GetMapping("/brands/{id}")
    public CatalogDTOs.BrandDTO getBrand(@PathVariable UUID id) {
        return catalog.getBrand(id);
    }

    // =========================
    // ADMIN
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogDTOs.CategoryDTO createCategory(@Valid @RequestBody CatalogDTOs.CreateNamedDTO dto) {
        return catalog.createCategory(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogDTOs.BrandDTO createBrand(@Valid @RequestBody CatalogDTOs.CreateNamedDTO dto) {
        return catalog.createBrand(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/categories/{id}")
    public CatalogDTOs.CategoryDTO updateCategory(@PathVariable UUID id,
                                                  @Valid @RequestBody CatalogDTOs.CreateNamedDTO dto) {
        return catalog.updateCategory(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/brands/{id}")
    public CatalogDTOs.BrandDTO updateBrand(@PathVariable UUID id,
                                            @Valid @RequestBody CatalogDTOs.CreateNamedDTO dto) {
        return catalog.updateBrand(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID id) {
        catalog.deleteCategory(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/brands/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable UUID id) {
        catalog.deleteBrand(id);
    }
}
