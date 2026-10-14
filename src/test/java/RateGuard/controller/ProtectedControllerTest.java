
package RateGuard.controller;

import RateGuard.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtectedControllerTest {

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        RateLimiterService service = mock(RateLimiterService.class);
        ProtectedController controller = new ProtectedController(service);

        when(service.recordRequest("student101")).thenReturn(1L);

        ResponseEntity<String> response = controller.protectedEndpoint();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("5", response.getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("4", response.getHeaders().getFirst("X-RateLimit-Remaining"));
    }
    
    @Test
    void shouldRejectRequestWhenLimitExceeded() {
        RateLimiterService service = mock(RateLimiterService.class);
        ProtectedController controller = new ProtectedController(service);

        when(service.recordRequest("student101")).thenReturn(6L);
        when(service.getRemainingWindowSeconds("student101")).thenReturn(28L);

        ResponseEntity<String> response = controller.protectedEndpoint();

        assertEquals(429, response.getStatusCode().value());
        assertEquals("5", response.getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("0", response.getHeaders().getFirst("X-RateLimit-Remaining"));
        assertEquals("28", response.getHeaders().getFirst("Retry-After"));
    }

}
