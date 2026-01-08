package it.ridfix.backend.dto;

import it.ridfix.backend.entities.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class UserDTOs {
    private UserDTOs() {}

    public record UserResponse(
            UUID id,
            String email,
            String name,
            String surname,
            Role role,
            String profileImageUrl
    ) {}

    public record UpdateUserRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(max = 80) String surname
    ) {}
}
