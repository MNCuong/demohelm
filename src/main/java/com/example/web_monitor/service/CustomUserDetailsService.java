package com.example.web_monitor.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.web_monitor.model.entities.RoleEntity;
import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; 

    CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm user trong DB
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username);
        }
        Set<GrantedAuthority> authorities = new HashSet<>();
        RoleEntity role = user.getRole();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        // 2. Chuyển đổi Role (từ DB) sang GrantedAuthority (của Spring Security)
        // Set<GrantedAuthority> authorities = new HashSet<>();
        // for (RoleEntity role : user.getRole()) {
        //     authorities.add(new SimpleGrantedAuthority(role.getName()));
        // }

        // 3. Trả về đối tượng UserDetails mà Spring Security hiểu
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(), // Password đã mã hóa trong DB
            user.isEnabled(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        );
    }
}