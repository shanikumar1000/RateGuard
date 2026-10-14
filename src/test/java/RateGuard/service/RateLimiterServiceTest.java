
package RateGuard.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimiterServiceTest {

    @Test
    void shouldAllowFiveRequestsAndRejectSixth() {
        StringRedisTemplate redisTemplate =
                mock(StringRedisTemplate.class);

        RateLimiterService service =
                new RateLimiterService(redisTemplate);

        
        

        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(Object[].class)))
                .thenReturn(1L, 2L, 3L, 4L, 5L, 6L);

        for (int i = 1; i <= 5; i++) {
            assertTrue(service.recordRequest("student101") <= 5,
                    "Request " + i + " should be allowed");
        }

        assertFalse(service.recordRequest("student101") <= 5,
                "Request 6 should be rejected");

    }
}
