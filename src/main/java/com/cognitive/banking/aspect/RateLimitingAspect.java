// src/main/java/com/cognitive/banking/aspect/RateLimitingAspect.java
package com.cognitive.banking.aspect;

import com.cognitive.banking.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitingAspect {

    // Key -> (lastResetTimestamp, currentCount, capacity)
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitStore = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientId = getClientId(request, rateLimit.key());

        RateLimitInfo info = rateLimitStore.computeIfAbsent(clientId,
                k -> new RateLimitInfo(rateLimit.capacity(), rateLimit.refillMinutes()));

        synchronized (info) {
            long now = Instant.now().getEpochSecond();
            long windowSeconds = rateLimit.refillMinutes() * 60L;

            // Reset if window has passed
            if (now - info.lastResetSeconds >= windowSeconds) {
                info.count = 0;
                info.lastResetSeconds = now;
            }

            if (info.count < info.capacity) {
                info.count++;
                return joinPoint.proceed();
            } else {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Rate limit exceeded. Max " + info.capacity + " requests per " +
                                rateLimit.refillMinutes() + " minute(s). Try again later.");
            }
        }
    }

    private String getClientId(HttpServletRequest request, String keyExpression) {
        String ip = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        if (keyExpression != null && !keyExpression.isEmpty()) {
            return ip + ":" + endpoint + ":" + keyExpression;
        }
        return ip + ":" + endpoint;
    }

    // Helper class to hold rate limit state
    private static class RateLimitInfo {
        final int capacity;
        long lastResetSeconds;
        int count;

        RateLimitInfo(int capacity, int refillMinutes) {
            this.capacity = capacity;
            this.lastResetSeconds = Instant.now().getEpochSecond();
            this.count = 0;
        }
    }
}