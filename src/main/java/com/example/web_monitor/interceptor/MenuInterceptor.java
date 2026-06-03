package com.example.web_monitor.interceptor;

import com.example.web_monitor.dto.MenuDto;
import com.example.web_monitor.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Component
public class MenuInterceptor implements HandlerInterceptor {

    @Autowired
    private MenuService menuService;

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

        // Kiểm tra xem user đã đăng nhập chưa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem:
        //    - User đã đăng nhập (authentication != null)
        //    - Có phải là một request đang render view không (modelAndView != null)
        //    - View đó không phải là một lệnh redirect (tránh xử lý thừa)
        if (authentication != null && modelAndView != null && !modelAndView.getViewName().startsWith("redirect:")) {
            
            List<MenuDto> sidebarMenus = menuService.getActiveMenuTree(authentication);

            modelAndView.addObject("sidebarMenus", sidebarMenus);
        }
    }
}