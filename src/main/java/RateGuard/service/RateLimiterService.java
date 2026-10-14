
package RateGuard.service;

import java.time.Duration;
import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private static final int REQUEST_LIMIT = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private static final String RATE_LIMIT_SCRIPT = """
            local count = redis.call('INCR', KEYS[1])

            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end

            return count
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(RATE_LIMIT_SCRIPT);
        this.rateLimitScript.setResultType(Long.class);
    }

    public long recordRequest(String requesterId) {
        String key = "rateguard:limit:" + requesterId;

        Long count = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(WINDOW.toSeconds()));

        if (count == null) {
            throw new IllegalStateException(
                    "Could not update request counter");
        }

        return count;
    }

    public long getRemainingWindowSeconds(String requesterId) {
        String key = "rateguard:limit:" + requesterId;

        Long remainingSeconds = redisTemplate.getExpire(key);

        if (remainingSeconds == null || remainingSeconds < 0) {
            return 0;
        }

        return remainingSeconds;
    }

}
