package com.example.web_monitor.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    // 1. Xác định ngôn ngữ hiện tại (Lưu vào Cookie để lần sau vào web vẫn nhớ)
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("USER_LANG");
        resolver.setDefaultLocale(new Locale("vi")); // Mặc định là Tiếng Việt
        resolver.setCookieMaxAge(Duration.ofDays(30)); // Nhớ trong 30 ngày
        return resolver;
    }

    // 2. Bộ chặn để đổi ngôn ngữ khi URL có tham số ?lang=en hoặc ?lang=vi
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    // 3. Đăng ký bộ chặn
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}