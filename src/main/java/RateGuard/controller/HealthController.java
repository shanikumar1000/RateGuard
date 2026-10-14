
package RateGuard.controller;
import RateGuard.model.HealthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final StringRedisTemplate redisTemplate;

    public HealthController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/api/health")
public ResponseEntity<HealthResponse> checkHealth() {
    try {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();

        return ResponseEntity.ok(
                new HealthResponse(
                        "UP",
                        "RateGuard is running.",
                        "CONNECTED"
                )
        );

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthResponse(
                        "DOWN",
                        "RateGuard is running, but Redis is unavailable.",
                        "DISCONNECTED"
                ));
    }
}

    
}
