package it.ridfix.backend.services;

import it.ridfix.backend.dto.CommonDTOs;
import it.ridfix.backend.dto.ProductDTOs;
import it.ridfix.backend.entities.Brand;
import it.ridfix.backend.entities.Category;
import it.ridfix.backend.entities.product.Accessory;
import it.ridfix.backend.entities.product.Product;
import it.ridfix.backend.entities.product.SparePart;
import it.ridfix.backend.exceptions.ApiExceptions;
import it.ridfix.backend.external.cloudinary.CloudinaryService;
import it.ridfix.backend.mappers.MapperUtils;
import it.ridfix.backend.repositories.BrandRepository;
import it.ridfix.backend.repositories.CategoryRepository;
import it.ridfix.backend.repositories.ProductRepository;
import it.ridfix.backend.repositories.ReviewRepository;
import it.ridfix.backend.specs.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ReviewRepository reviews;
    private final CloudinaryService cloudinary;

    public ProductService(ProductRepository products,
                          CategoryRepository categories,
                          BrandRepository brands,
                          ReviewRepository reviews,
                          CloudinaryService cloudinary) {
        this.products = products;
        this.categories = categories;
        this.brands = brands;
        this.reviews = reviews;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public CommonDTOs.PageResponse<ProductDTOs.ProductResponse> list(String q,
                                                                     UUID categoryId,
                                                                     UUID brandId,
                                                                     BigDecimal minPrice,
                                                                     BigDecimal maxPrice,
                                                                     Boolean inStock,
                                                                     Boolean activeOnly,
                                                                     int page,
                                                                     int size,
                                                                     String sort) {

        Sort s = parseSort(sort);
        PageRequest pr = PageRequest.of(page, size, s);

        Page<Product> result = products.findAll(ProductSpecifications.build(q, categoryId, brandId, minPrice, maxPrice, inStock, activeOnly), pr);

        var mapped = result.getContent().stream().map(p -> {
            ReviewRepository.RatingAggRow agg = reviews.ratingAgg(p.getId());
            double avg = (agg != null && agg.getAvgRating() != null) ? agg.getAvgRating() : 0.0;
            long cnt = (agg != null && agg.getCount() != null) ? agg.getCount() : 0L;
            return MapperUtils.productToResponse(p, avg, cnt);
        }).toList();

        return new CommonDTOs.PageResponse<>(mapped, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductDTOs.ProductResponse getById(UUID id) {
        Product p = products.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("Product not found"));
        ReviewRepository.RatingAggRow agg = reviews.ratingAgg(p.getId());
        double avg = (agg != null && agg.getAvgRating() != null) ? agg.getAvgRating() : 0.0;
        long cnt = (agg != null && agg.getCount() != null) ? agg.getCount() : 0L;
        return MapperUtils.productToResponse(p, avg, cnt);
    }

    @Transactional
    public ProductDTOs.ProductResponse create(ProductDTOs.ProductCreateRequest req) {
        Category c = categories.findById(req.categoryId()).orElseThrow(() -> new ApiExceptions.NotFound("Category not found"));
        Brand b = brands.findById(req.brandId()).orElseThrow(() -> new ApiExceptions.NotFound("Brand not found"));

        Product p;
        String type = req.productType().toUpperCase();
        if (type.equals("SPARE_PART")) {
            p = new SparePart(req.name(), req.description(), req.price(), req.stockQty(), c, b, req.oemCode(), req.compatibility());
        } else if (type.equals("ACCESSORY")) {
            p = new Accessory(req.name(), req.description(), req.price(), req.stockQty(), c, b, req.material(), req.color());
        } else {
            throw new ApiExceptions.BadRequest("Invalid productType: use SPARE_PART or ACCESSORY");
        }

        products.save(p);
        return getById(p.getId());
    }

    @Transactional
    public ProductDTOs.ProductResponse update(UUID id, ProductDTOs.ProductUpdateRequest req) {
        Product p = products.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("Product not found"));

        if (req.name() != null) p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.price() != null) p.setPrice(req.price());
        if (req.stockQty() != null) p.setStockQty(req.stockQty());
        if (req.active() != null) p.setActive(req.active());

        if (req.categoryId() != null) {
            Category c = categories.findById(req.categoryId()).orElseThrow(() -> new ApiExceptions.NotFound("Category not found"));
            p.setCategory(c);
        }
        if (req.brandId() != null) {
            Brand b = brands.findById(req.brandId()).orElseThrow(() -> new ApiExceptions.NotFound("Brand not found"));
            p.setBrand(b);
        }

        // subtype updates
        if (p instanceof SparePart sp) {
            if (req.oemCode() != null) sp.setOemCode(req.oemCode());
            if (req.compatibility() != null) sp.setCompatibility(req.compatibility());
        }
        if (p instanceof Accessory ac) {
            if (req.material() != null) ac.setMaterial(req.material());
            if (req.color() != null) ac.setColor(req.color());
        }

        return getById(p.getId());
    }

    @Transactional
    public ProductDTOs.ProductResponse uploadImage(UUID id, MultipartFile file) {
        Product p = products.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("Product not found"));
        String url = cloudinary.uploadImage(file);
        p.setImageUrl(url);
        return getById(id);
    }

    @Transactional
    public void delete(UUID id) {
        Product p = products.findById(id).orElseThrow(() -> new ApiExceptions.NotFound("Product not found"));
        products.delete(p);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        // Example: "price,asc" or "name,desc"
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "price" -> Sort.by(dir, "price");
            case "name" -> Sort.by(dir, "name");
            case "stockQty" -> Sort.by(dir, "stockQty");
            case "createdAt" -> Sort.by(dir, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
