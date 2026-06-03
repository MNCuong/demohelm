package com.example.web_monitor.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.web_monitor.interceptor.MenuInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private MenuInterceptor menuInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(menuInterceptor)
                // Áp dụng cho tất cả các đường dẫn...
                .addPathPatterns("/**")
                // ...TRỪ các đường dẫn sau:
                .excludePathPatterns(
                        "/api/**",  // Bỏ qua tất cả API
                        "/login",   // Bỏ qua trang login
                        "/error",   // Bỏ qua trang lỗi
                        "/css/**",  // Bỏ qua file tĩnh
                        "/js/**",
                        "/images/**"
                );
    }
}