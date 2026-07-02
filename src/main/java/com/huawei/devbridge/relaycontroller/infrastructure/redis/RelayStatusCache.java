package com.huawei.devbridge.relaycontroller.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.devbridge.relaycontroller.domain.model.RelayStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RelayStatusCache {
    private static final String KEY_PREFIX = "relay:";
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RelayStatus get(String tunnelId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + tunnelId);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, RelayStatus.class);
        } catch (RuntimeException exception) {
            return null;
        } catch (Exception exception) {
            return null;
        }
    }
}
