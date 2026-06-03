package com.example.web_monitor.service;

import com.example.web_monitor.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HealthcheckAsyncService {

    private final HttpClientUtil http;

    public HealthcheckAsyncService(HttpClientUtil http) {
        this.http = http;
    }

    @Async
    public CompletableFuture<Map<String, Object>> check(
            String path,
            String host
    ) {
        try {
            Map<String, Object> result = http.callApi(
                    host + path,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            return CompletableFuture.completedFuture(result);
        } catch (Exception ex) {
            log.warn("Healthcheck unreachable: {}", ex.getMessage());
            return CompletableFuture.completedFuture(
                    Map.of(
                            "status", "UNKNOWN",
                            "healthcheckError", true
                    )
            );
        }
    }
}
