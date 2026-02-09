package it.ridfix.backend.exceptions;

import it.ridfix.backend.dto.CommonDTOs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiExceptions.ApiException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleApi(ApiExceptions.ApiException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getStatus().getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
    }

    // ✅ 403 quando @PreAuthorize blocca (prima ti diventava 500)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), "Forbidden", req.getRequestURI(), null);
    }

    // ✅ 400 quando JSON è rotto o enum non valido (es. status non matcha OrderStatus)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        // Non sparare stacktrace o dettagli inutili: messaggio pulito per Postman / frontend
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON", "Invalid request body", req.getRequestURI(), null);
    }

    // ✅ 400 quando path/query param non è convertibile (es. UUID non valido)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String name = ex.getName();
        String value = ex.getValue() == null ? "null" : ex.getValue().toString();
        String msg = "Invalid value for parameter '" + name + "': " + value;
        return build(HttpStatus.BAD_REQUEST, "Type mismatch", msg, req.getRequestURI(), null);
    }

    // ✅ 400 per @Valid su body (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<CommonDTOs.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Invalid request", req.getRequestURI(), fields);
    }

    // ✅ 400 per validazioni su query params / path params quando usi @Validated + @Min/@Max ecc.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        // Qui non hai field name puliti come in MethodArgumentNotValidException: restituiamo un messaggio compatto
        return build(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), req.getRequestURI(), null);
    }

    // ✅ 409 per vincoli DB (unique, FK, ecc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Data integrity violation", "Conflict with existing data", req.getRequestURI(), null);
    }

    // ✅ 400 se manca un parametro obbligatorio
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Missing required parameter: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, "Missing parameter", msg, req.getRequestURI(), null);
    }

    // ✅ 405 su metodo HTTP sbagliato (evita 500)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonDTOs.ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", ex.getMessage(), req.getRequestURI(), null);
    }

    // Fallback (solo per bug veri)
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
