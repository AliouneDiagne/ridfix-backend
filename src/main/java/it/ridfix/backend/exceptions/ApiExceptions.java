package it.ridfix.backend.exceptions;

import org.springframework.http.HttpStatus;

public final class ApiExceptions {
    private ApiExceptions() {}

    public static abstract class ApiException extends RuntimeException {
        private final HttpStatus status;

        protected ApiException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

    public static class BadRequest extends ApiException {
        public BadRequest(String message) { super(HttpStatus.BAD_REQUEST, message); }
    }

    public static class Unauthorized extends ApiException {
        public Unauthorized(String message) { super(HttpStatus.UNAUTHORIZED, message); }
    }

    public static class Forbidden extends ApiException {
        public Forbidden(String message) { super(HttpStatus.FORBIDDEN, message); }
    }

    public static class NotFound extends ApiException {
        public NotFound(String message) { super(HttpStatus.NOT_FOUND, message); }
    }

    public static class Conflict extends ApiException {
        public Conflict(String message) { super(HttpStatus.CONFLICT, message); }
    }

    public static class ExternalService extends ApiException {
        public ExternalService(String message) { super(HttpStatus.BAD_GATEWAY, message); }
    }
}
