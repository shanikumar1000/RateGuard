
package RateGuard.controller;
import RateGuard.model.ApiResponse;
import RateGuard.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProtectedControllerTest {

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        RateLimiterService service = mock(RateLimiterService.class);
        ProtectedController controller = new ProtectedController(service);
        ReflectionTestUtils.setField(
        controller, "expectedApiKey", "rg-dev-key-2026-change-this");

        when(service.recordRequest("clientA")).thenReturn(1L);

        ResponseEntity<ApiResponse> response =
        controller.protectedEndpoint("clientA", "rg-dev-key-2026-change-this");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("5", response.getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("4", response.getHeaders().getFirst("X-RateLimit-Remaining"));
    }
    
    @Test
    void shouldRejectRequestWhenLimitExceeded() {
        RateLimiterService service = mock(RateLimiterService.class);
        ProtectedController controller = new ProtectedController(service);
        ReflectionTestUtils.setField(
        controller, "expectedApiKey", "rg-dev-key-2026-change-this");

        when(service.recordRequest("clientA")).thenReturn(6L);
        when(service.getRemainingWindowSeconds("clientA")).thenReturn(28L);

        ResponseEntity<ApiResponse> response =
        controller.protectedEndpoint("clientA", "rg-dev-key-2026-change-this");

        assertEquals(429, response.getStatusCode().value());
        assertEquals("5", response.getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("0", response.getHeaders().getFirst("X-RateLimit-Remaining"));
        assertEquals("28", response.getHeaders().getFirst("Retry-After"));
        assertNotNull(response.getHeaders().getFirst("X-RateLimit-Reset"));
    }
    @Test
    void shouldRejectInvalidClientId() {
        RateLimiterService service = mock(RateLimiterService.class);
        ProtectedController controller = new ProtectedController(service);
        ReflectionTestUtils.setField(
        controller, "expectedApiKey", "rg-dev-key-2026-change-this");

        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.protectedEndpoint("client A", "rg-dev-key-2026-change-this")
        );

        assertEquals(400, exception.getStatusCode().value());
    }
    @Test
    void shouldRejectRequestWhenClientIdHeaderIsMissing() throws Exception {
        RateLimiterService service = mock(RateLimiterService.class);
        ProtectedController controller = new ProtectedController(service);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isBadRequest());
    }
@Test
void shouldRejectInvalidApiKey() {
    RateLimiterService service = mock(RateLimiterService.class);
    ProtectedController controller = new ProtectedController(service);

    ReflectionTestUtils.setField(
            controller, "expectedApiKey", "rg-dev-key-2026-change-this");

    ResponseEntity<ApiResponse> response =
            controller.protectedEndpoint("clientA", "wrong-api-key");

    assertEquals(401, response.getStatusCode().value());
    assertEquals(
            "Invalid or missing API key.",
            response.getBody().getMessage());
}
}
