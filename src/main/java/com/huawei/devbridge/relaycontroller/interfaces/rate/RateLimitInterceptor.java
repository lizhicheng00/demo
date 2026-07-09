package com.huawei.devbridge.relaycontroller.interfaces.rate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final long WINDOW_MILLIS = 60_000L;
    private static final long COUNTER_TTL_MILLIS = WINDOW_MILLIS * 2;
    private static final int MAX_COUNTERS = 4096;
    private static final String NAMESPACE_HEADER = "X-Namespace";
    private static final String OVERFLOW_KEY = "overflow";

    private final RelayProperties relayProperties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private volatile long lastCleanupAt;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        RelayProperties.RateLimit rateLimit = relayProperties.getRateLimit();
        if (!rateLimit.isEnabled() || rateLimit.getRequestsPerMinute() <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        cleanupCounters(now);
        WindowCounter counter = counters.computeIfAbsent(counterKey(request), ignored -> new WindowCounter());
        if (counter.allow(now, rateLimit.getRequestsPerMinute())) {
            return true;
        }
        writeRateLimitedResponse(response);
        return false;
    }

    private String counterKey(HttpServletRequest request) {
        String key = rateKey(request);
        if (counters.size() >= MAX_COUNTERS && !counters.containsKey(key)) {
            return OVERFLOW_KEY;
        }
        return key;
    }

    private String rateKey(HttpServletRequest request) {
        String namespace = request.getHeader(NAMESPACE_HEADER);
        if (namespace != null && !namespace.isBlank()) {
            return "namespace:" + namespace.trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private void cleanupCounters(long now) {
        if (now - lastCleanupAt < WINDOW_MILLIS && counters.size() < MAX_COUNTERS) {
            return;
        }
        lastCleanupAt = now;
        counters.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private void writeRateLimitedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.failure(ErrorCode.RATE_LIMITED));
    }

    private static final class WindowCounter {
        private long windowStart;
        private long lastSeen;
        private int count;

        private synchronized boolean allow(long now, int limit) {
            lastSeen = now;
            if (now - windowStart >= WINDOW_MILLIS) {
                windowStart = now;
                count = 0;
            }
            count++;
            return count <= limit;
        }

        private synchronized boolean isExpired(long now) {
            return lastSeen > 0 && now - lastSeen > COUNTER_TTL_MILLIS;
        }
    }
}
