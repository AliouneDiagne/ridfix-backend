package it.ridfix.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ProductDTOs {
    private ProductDTOs() {}

    public record ProductCreateRequest(
            @NotBlank String productType,                // SPARE_PART | ACCESSORY
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @Min(0) int stockQty,
            @NotNull UUID categoryId,
            @NotNull UUID brandId,
            // subtype fields (nullable)
            String oemCode,
            String compatibility,
            String material,
            String color
    ) {}

    public record ProductUpdateRequest(
            @Size(max = 160) String name,
            @Size(max = 2000) String description,
            @DecimalMin("0.00") BigDecimal price,
            Integer stockQty,
            Boolean active,
            UUID categoryId,
            UUID brandId,
            String oemCode,
            String compatibility,
            String material,
            String color
    ) {}

    public record ProductResponse(
            UUID id,
            String productType,
            String name,
            String description,
            BigDecimal price,
            int stockQty,
            boolean inStock,
            boolean active,
            String imageUrl,
            UUID categoryId,
            String categoryName,
            UUID brandId,
            String brandName,
            Double avgRating,
            long reviewCount,
            Instant createdAt
    ) {}
}
