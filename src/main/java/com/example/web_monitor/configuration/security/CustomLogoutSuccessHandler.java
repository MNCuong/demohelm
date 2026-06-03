package com.example.web_monitor.configuration.security;

import com.example.web_monitor.service.MenuService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final MenuService menuService;

    public CustomLogoutSuccessHandler(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (authentication != null) {
            menuService.evictUserMenuCache(authentication);
        }

        response.sendRedirect("/login?logout=true");
    }

}
