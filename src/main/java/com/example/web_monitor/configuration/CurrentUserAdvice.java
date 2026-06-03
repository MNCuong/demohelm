package com.example.web_monitor.configuration;

import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice
// Lớp này sẽ cung cấp thông tin user hiện tại cho tất cả các controller
public class CurrentUserAdvice {
    private final UserRepository userRepository ;

    public CurrentUserAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public UserEntity currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String username;
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = auth.getName();
        }
        return userRepository.findByUsername(username);
    }
}
