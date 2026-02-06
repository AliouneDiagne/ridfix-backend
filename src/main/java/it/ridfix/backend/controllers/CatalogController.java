package it.ridfix.backend.controllers;

import it.ridfix.backend.dto.CatalogDTOs;
import it.ridfix.backend.services.CatalogService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<CatalogDTOs.CategoryDTO> categories() {
        return catalog.listCategories();
    }

    @GetMapping("/brands")
    public List<CatalogDTOs.BrandDTO> brands() {
        return catalog.listBrands();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    public CatalogDTOs.CategoryDTO createCategory(@Valid @RequestBody CatalogDTOs.CreateNamedDTO dto) {
        return catalog.createCategory(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/brands")
    public CatalogDTOs.BrandDTO createBrand(@Valid @RequestBody CatalogDTOs.CreateNamedDTO dto) {
        return catalog.createBrand(dto);
    }
}