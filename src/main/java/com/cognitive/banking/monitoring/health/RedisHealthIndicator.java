package com.cognitive.banking.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            // Use explicit RedisCallback to avoid ambiguity
            String response = redisTemplate.execute((RedisCallback<String>) connection -> {
                byte[] pong = connection.ping().getBytes();
                return new String(pong);
            });

            if ("PONG".equals(response)) {
                return Health.up()
                        .withDetail("status", "available")
                        .withDetail("connection", "successful")
                        .build();
            }

            return Health.down()
                    .withDetail("status", "unhealthy")
                    .withDetail("response", response)
                    .build();

        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("status", "down")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}