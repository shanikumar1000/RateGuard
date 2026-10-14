
package RateGuard.controller;
import RateGuard.model.HealthResponse;
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
public HealthResponse checkHealth() {
    return new HealthResponse("UP", "RateGuard is running.");
}

    @GetMapping("/api/redis-test")
    public String testRedis() {
        redisTemplate.opsForValue().set("rateguard:test", "Redis connected successfully!");

        String result = redisTemplate.opsForValue().get("rateguard:test");

        return result;
    }
}
