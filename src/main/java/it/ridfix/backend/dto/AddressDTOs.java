package it.ridfix.backend.dto;

import it.ridfix.backend.entities.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AddressDTOs {
    private AddressDTOs() {}

    public record AddressRequest(
            @NotNull AddressType type,
            @NotBlank @Size(max = 120) String street,
            @NotBlank @Size(max = 80) String city,
            @NotBlank @Size(max = 16) String postalCode,
            @NotBlank @Size(max = 80) String country,
            boolean isDefault
    ) {}

    public record AddressResponse(
            UUID id,
            AddressType type,
            String street,
            String city,
            String postalCode,
            String country,
            boolean isDefault
    ) {}
}
