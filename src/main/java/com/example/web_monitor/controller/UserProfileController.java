package com.example.web_monitor.controller;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.dto.UserDto;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class UserProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        // 1. Lấy username của người đang đăng nhập
        // Principal là interface của Java Security chứ thông tin user hiện tại
        String currentUsername = principal.getName();

        // 2. Tìm thông tin chi tiết từ DB
        UserDto userProfile = userService.findByUsername(currentUsername);

        // 3. Đẩy dữ liệu ra view
        model.addAttribute("user", userProfile);

        return "pages/users/profile"; // Trả về file HTML
    }

    @PostMapping("/profile/update")
    @TrackAction(ActionType.EDIT_USER)
    public String updateProfile(@ModelAttribute("user") UserDto userDto, RedirectAttributes redirectAttributes) {
        boolean success = userService.updateProfile(userDto);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Profile update failed or user does not exist!");
        }
        return "redirect:/profile";
    }

}
