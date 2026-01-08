package it.ridfix.backend.exceptions;

import it.ridfix.backend.dto.CommonDTOs;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiExceptions.ApiException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleApi(ApiExceptions.ApiException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getStatus().getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<CommonDTOs.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Invalid request", req.getRequestURI(), fields);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Data integrity violation", "Conflict with existing data", req.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonDTOs.ApiError> handleOther(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", ex.getMessage(), req.getRequestURI(), null);
    }

    private CommonDTOs.FieldError toFieldError(FieldError fe) {
        return new CommonDTOs.FieldError(fe.getField(), fe.getDefaultMessage());
    }

    private ResponseEntity<CommonDTOs.ApiError> build(HttpStatus status,
                                                      String error,
                                                      String message,
                                                      String path,
                                                      List<CommonDTOs.FieldError> fieldErrors) {
        CommonDTOs.ApiError body = new CommonDTOs.ApiError(
                Instant.now(),
                status.value(),
                error,
                message,
                path,
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
