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
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public CatalogDTOs.CategoryDTO getCategory(UUID id) {
        Category c = categories.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("Category not found"));
        return new CatalogDTOs.CategoryDTO(c.getId(), c.getName());
    }

    @Transactional(readOnly = true)
    public CatalogDTOs.BrandDTO getBrand(UUID id) {
        Brand b = brands.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("Brand not found"));
        return new CatalogDTOs.BrandDTO(b.getId(), b.getName());
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

    @Transactional
    public CatalogDTOs.CategoryDTO updateCategory(UUID id, CatalogDTOs.CreateNamedDTO dto) {
        Category c = categories.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("Category not found"));

        String newName = dto.name().trim();
        if (!c.getName().equalsIgnoreCase(newName) && categories.existsByNameIgnoreCase(newName)) {
            throw new ApiExceptions.Conflict("Category already exists");
        }

        c.setName(newName);
        return new CatalogDTOs.CategoryDTO(c.getId(), c.getName());
    }

    @Transactional
    public CatalogDTOs.BrandDTO updateBrand(UUID id, CatalogDTOs.CreateNamedDTO dto) {
        Brand b = brands.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("Brand not found"));

        String newName = dto.name().trim();
        if (!b.getName().equalsIgnoreCase(newName) && brands.existsByNameIgnoreCase(newName)) {
            throw new ApiExceptions.Conflict("Brand already exists");
        }

        b.setName(newName);
        return new CatalogDTOs.BrandDTO(b.getId(), b.getName());
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category c = categories.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("Category not found"));
        categories.delete(c);
    }

    @Transactional
    public void deleteBrand(UUID id) {
        Brand b = brands.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFound("Brand not found"));
        brands.delete(b);
    }
}
