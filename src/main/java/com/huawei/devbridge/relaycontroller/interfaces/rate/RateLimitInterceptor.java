package com.huawei.devbridge.relaycontroller.interfaces.rate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.infrastructure.config.RelayProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final long WINDOW_MILLIS = 60_000L;
    private static final String NAMESPACE_HEADER = "X-Namespace";

    private final RelayProperties relayProperties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        RelayProperties.RateLimit rateLimit = relayProperties.getRateLimit();
        if (!rateLimit.isEnabled() || rateLimit.getRequestsPerMinute() <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        String key = rateKey(request);
        WindowCounter counter = counters.compute(key, (ignored, current) -> current == null || current.expired(now)
                ? new WindowCounter(now, WINDOW_MILLIS)
                : current);
        if (counter.increment() <= rateLimit.getRequestsPerMinute()) {
            return true;
        }
        writeRateLimitedResponse(response);
        return false;
    }

    private String rateKey(HttpServletRequest request) {
        String namespace = request.getHeader(NAMESPACE_HEADER);
        if (namespace != null && !namespace.isBlank()) {
            return "namespace:" + namespace.trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private void writeRateLimitedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.failure(ErrorCode.RATE_LIMITED));
    }

    private static final class WindowCounter {
        private final long expiresAt;
        private final AtomicInteger count = new AtomicInteger();

        private WindowCounter(long createdAt, long ttlMillis) {
            this.expiresAt = createdAt + ttlMillis;
        }

        private int increment() {
            return count.incrementAndGet();
        }

        private boolean expired(long now) {
            return now >= expiresAt;
        }
    }
}
