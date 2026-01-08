package it.ridfix.backend.services;

import it.ridfix.backend.dto.CatalogDTOs;
import it.ridfix.backend.entities.Brand;
import it.ridfix.backend.entities.Category;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.repositories.BrandRepository;
import it.ridfix.backend.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final BrandRepository brands;

    public CatalogService(CategoryRepository categories, BrandRepository brands) {
        this.categories = categories;
        this.brands = brands;
    }

    @Transactional(readOnly = true)
    public List<CatalogDTOs.CategoryDTO> listCategories() {
        return categories.findAll().stream()
                .map(c -> new CatalogDTOs.CategoryDTO(c.getId(), c.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDTOs.BrandDTO> listBrands() {
        return brands.findAll().stream()
                .map(b -> new CatalogDTOs.BrandDTO(b.getId(), b.getName()))
                .toList();
    }

    @Transactional
    public CatalogDTOs.CategoryDTO createCategory(CatalogDTOs.CreateNamedDTO dto) {
        if (categories.existsByNameIgnoreCase(dto.name())) {
            throw new ApiExceptions.Conflict("Category already exists");
        }
        Category c = categories.save(new Category(dto.name()));
        return new CatalogDTOs.CategoryDTO(c.getId(), c.getName());
    }

    @Transactional
    public CatalogDTOs.BrandDTO createBrand(CatalogDTOs.CreateNamedDTO dto) {
        if (brands.existsByNameIgnoreCase(dto.name())) {
            throw new ApiExceptions.Conflict("Brand already exists");
        }
        Brand b = brands.save(new Brand(dto.name()));
        return new CatalogDTOs.BrandDTO(b.getId(), b.getName());
    }
}
