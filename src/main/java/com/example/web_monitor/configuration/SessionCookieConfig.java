package com.example.web_monitor.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

@Configuration
public class SessionCookieConfig {

    @Value("${server.servlet.session.timeout}")
    private Duration sessionTimeout;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        serializer.setCookieName("WEB_MONITOR_SESSION");
        serializer.setCookiePath("/");
        serializer.setCookieMaxAge((int) sessionTimeout.getSeconds());
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax");
        serializer.setUseSecureCookie(false);

        return serializer;
    }
}
