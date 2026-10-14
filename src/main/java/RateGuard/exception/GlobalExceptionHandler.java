package RateGuard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(
            MissingRequestHeaderException exception) {

        Map<String, Object> error = Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "X-Client-Id header is required"
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidClientId(
            ResponseStatusException exception) {

        Map<String, Object> error = Map.of(
                "status", exception.getStatusCode().value(),
                "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "message", exception.getReason() != null
                        ? exception.getReason()
                        : "Invalid request"
        );

        return ResponseEntity.status(exception.getStatusCode()).body(error);
    }
}