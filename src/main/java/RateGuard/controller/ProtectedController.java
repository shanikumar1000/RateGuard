
package RateGuard.controller;

import RateGuard.service.RateLimiterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

    private static final long REQUEST_LIMIT = 5;

    private final RateLimiterService rateLimiterService;

    public ProtectedController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/protected")
    public ResponseEntity<String> protectedEndpoint() {

        String requesterId = "student101";

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

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body("Rate limit exceeded. Try again later.");
        }

        return ResponseEntity
                .ok()
                .headers(headers)
                .body("Request allowed! Welcome to RateGuard.");
    }
}
