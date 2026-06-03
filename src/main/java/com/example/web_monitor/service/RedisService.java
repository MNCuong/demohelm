package com.example.web_monitor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.Cursor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    @Value("${app.banned.ttl.minutes}")
    private long bannedTtlMinutes;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Page<String> scanClientLoginKeysAsPage(
            int page,
            int size,
            String search
    ) {
        try {
            List<String> matchedKeys = new ArrayList<>();
            String matchPattern = "client:login:*";
            ScanOptions options = ScanOptions.scanOptions()
                    .match(matchPattern)
                    .count(1000)
                    .build();

            RedisConnection connection =
                    redisTemplate.getConnectionFactory().getConnection();
            ObjectMapper mapper = new ObjectMapper();

            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    String key = new String(cursor.next(), StandardCharsets.UTF_8);
                    String value = redisTemplate.opsForValue().get(key);

                    boolean matches;

                    if (search == null || search.isBlank()) {
                        matches = true;
                    } else if (key.contains(search)) {
                        matches = true;
                    } else if (value != null && !value.isBlank()) {
                        try {
                            Map<String, Object> jsonMap =
                                    mapper.readValue(value, new TypeReference<>() {});
                            matches = jsonMap.values().stream()
                                    .anyMatch(v -> v != null && v.toString().contains(search));
                        } catch (JsonProcessingException e) {
                            continue;
                        }
                    } else {
                        matches = false;
                    }

                    if (matches) {
                        matchedKeys.add(key);
                    }
                }
            }

            int total = matchedKeys.size();

            int fromIndex = Math.min((page - 1) * size, total);
            int toIndex   = Math.min(fromIndex + size, total);

            List<String> pageData =
                    matchedKeys.subList(fromIndex, toIndex);

            Pageable pageable = PageRequest.of(page - 1, size);

            return new PageImpl<>(pageData, pageable, total);

        } catch (Exception ex) {
            log.warn("Redis unavailable, return empty page", ex);
            return Page.empty(PageRequest.of(page - 1, size));
        }
    }
    public Page<String> scanClientBannedKeysAsPage(
            int page,
            int size,
            String search
    ) {
        try {
            List<String> matchedKeys = new ArrayList<>();
            String matchPattern = "client:banned:*";

            ScanOptions options = ScanOptions.scanOptions()
                    .match(matchPattern)
                    .count(1000)
                    .build();

            RedisConnection connection =
                    redisTemplate.getConnectionFactory().getConnection();

            ObjectMapper mapper = new ObjectMapper();

            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    String key = new String(cursor.next(), StandardCharsets.UTF_8);
                    String value = redisTemplate.opsForValue().get(key);

                    boolean matches;

                    if (search == null || search.isBlank()) {
                        matches = true;
                    } else if (key.contains(search)) {
                        matches = true;
                    } else if (value != null && !value.isBlank()) {
                        try {
                            Map<String, Object> jsonMap =
                                    mapper.readValue(value, new TypeReference<>() {});
                            matches = jsonMap.values().stream()
                                    .anyMatch(v -> v != null && v.toString().contains(search));
                        } catch (JsonProcessingException e) {
                            continue;
                        }
                    } else {
                        matches = false;
                    }

                    if (matches) {
                        matchedKeys.add(key);
                    }
                }
            }

            // 👉 TOTAL THẬT
            int total = matchedKeys.size();

            int fromIndex = Math.min((page - 1) * size, total);
            int toIndex   = Math.min(fromIndex + size, total);

            List<String> pageData =
                    matchedKeys.subList(fromIndex, toIndex);

            Pageable pageable = PageRequest.of(page - 1, size);

            return new PageImpl<>(pageData, pageable, total);

        } catch (Exception ex) {
            log.warn("Redis unavailable, return empty page", ex);
            return Page.empty(PageRequest.of(page - 1, size));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public boolean kickParticipant(String clientKey) {
        try {
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(clientKey))) {
                return false;
            }
            String username = extractUsername(clientKey);

            if (username == null) {
                return false;
            }
            redisTemplate.delete(clientKey);
            String bannedKey = "client:banned:" + username;
            redisTemplate.opsForValue()
                    .set(bannedKey, "", bannedTtlMinutes, TimeUnit.MINUTES);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractUsername(String clientKey) {
        if (clientKey == null) return null;

        String prefix = "client:login:";
        if (!clientKey.startsWith(prefix)) return null;

        return clientKey.substring(prefix.length());
    }
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public boolean activeParticipant(String clientKey) {
        try {
            redisTemplate.delete(clientKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
