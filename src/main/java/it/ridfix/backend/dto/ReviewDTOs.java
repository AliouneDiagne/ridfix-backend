package it.ridfix.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ReviewDTOs {
    private ReviewDTOs() {}

    public record ReviewCreateRequest(
            @Min(1) @Max(5) int rating,
            @Size(max = 1000) String comment
    ) {}

    public record ReviewResponse(
            UUID id,
            UUID userId,
            String userName,
            int rating,
            String comment,
            Instant createdAt
    ) {}
}
