package com.example.web_monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginPage() {
        // Nếu user đã login, Spring Security sẽ tự động chuyển hướng
        // nên chúng ta chỉ cần trả về tên view
        return "pages/common/login"; // Trả về file login.html
    }
}
