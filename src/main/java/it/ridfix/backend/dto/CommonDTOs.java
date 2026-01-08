package it.ridfix.backend.dto;

import java.time.Instant;
import java.util.List;

public final class CommonDTOs {
    private CommonDTOs() {}

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<FieldError> fieldErrors
    ) {}

    public record FieldError(String field, String message) {}

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record SimpleMessage(String message) {}
}
