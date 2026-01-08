package it.ridfix.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class CatalogDTOs {
    private CatalogDTOs() {}

    public record CategoryDTO(UUID id, String name) {}
    public record BrandDTO(UUID id, String name) {}

    public record CreateNamedDTO(
            @NotBlank @Size(max = 80) String name
    ) {}
}
