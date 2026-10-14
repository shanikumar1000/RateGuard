
package RateGuard.controller;
import RateGuard.model.ApiResponse;
import RateGuard.service.RateLimiterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProtectedController {

    private static final long REQUEST_LIMIT = 5;

    private final RateLimiterService rateLimiterService;

    public ProtectedController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/protected")
    public ResponseEntity<ApiResponse> protectedEndpoint(
        @RequestHeader("X-Client-Id") String requesterId) {
            if (requesterId == null || !requesterId.matches("[a-zA-Z0-9_-]{1,50}")) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid X-Client-Id. Use 1-50 letters, numbers, hyphens, or underscores."
    );
}

        

        long count = rateLimiterService.recordRequest(requesterId);

        long remaining = Math.max(0, REQUEST_LIMIT - count);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Limit", String.valueOf(REQUEST_LIMIT));
        headers.set("X-RateLimit-Remaining", String.valueOf(remaining));

        if (count > REQUEST_LIMIT) {
            long retryAfter = rateLimiterService
                    .getRemainingWindowSeconds(requesterId);

            headers.set("Retry-After",
                    String.valueOf(Math.max(1, retryAfter)));
                    headers.set("X-RateLimit-Reset",
            String.valueOf(System.currentTimeMillis() / 1000 + retryAfter));

            return ResponseEntity
        .status(HttpStatus.TOO_MANY_REQUESTS)
        .headers(headers)
        .body(new ApiResponse(
                "Rate limit exceeded. Try again later.",
                requesterId,
                remaining
        ));
        }

        return ResponseEntity
        .ok()
        .headers(headers)
        .body(new ApiResponse(
                "Request allowed! Welcome to RateGuard.",
                requesterId,
                remaining
        ));
    }
}
