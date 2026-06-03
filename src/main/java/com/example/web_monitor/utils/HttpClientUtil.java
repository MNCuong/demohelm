package com.example.web_monitor.utils;

import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class HttpClientUtil {

    private final RestTemplate restTemplate;

    public HttpClientUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gọi API dùng chung
     */
    @Retryable(
            value = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public <T> T callApi(
            String url,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        try {
            return restTemplate
                    .exchange(url, method, entity, responseType)
                    .getBody();
        }

        // 4xx / 5xx có body
        catch (HttpStatusCodeException ex) {
            log.warn("API {} {} returned status {}", method, url, ex.getStatusCode());

            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(ex.getResponseBodyAsString(), responseType);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.HOST_RESPONSE_INVALID);
            }
        }

        catch (ResourceAccessException ex) {
            if (Map.class.isAssignableFrom(responseType)) {
                log.warn("Healthcheck unreachable: {}", url);
                @SuppressWarnings("unchecked")
                T fallback = (T) Map.of(
                        "status", "UNKNOWN",
                        "healthcheckError", true
                );
                return fallback;
            }
            throw new BusinessException(ErrorCode.HOST_TIMEOUT);
        }
    }


}
