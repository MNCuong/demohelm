package com.example.web_monitor.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.web_monitor.configuration.LdapConfig;
import com.example.web_monitor.model.entities.RoleEntity;
import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Value("${com.fss.esb.login.ldap.mode:false}")
    private boolean usingLDAP;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LdapConfig ldap;

    /**
     * các bước check hiện tại
     * 1. user db có tồn tại và enabled không
     * 2. nếu mode ldap -> chekc password trên ldap, ngược lại check password trên db
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        log.info("Attempting to authenticate user: {}", username);

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            log.warn("User not found in database: {}", username);
            throw new BadCredentialsException("Username hoặc password không đúng");
        }
        if (!user.isEnabled()) {
            log.warn("User account is disabled: {}", username);
            throw new BadCredentialsException("Tài khoản này đã bị vô hiệu hóa");
        }
        if (usingLDAP && !ldap.loginByLDAP(username, password)  ) {
            log.warn("LDAP authentication failed for user: {}", username);
            throw new BadCredentialsException("LDAP authentication failed");
        }

        
        if (!usingLDAP && !passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Username hoặc password ở DB không đúng");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        RoleEntity role = user.getRole();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            log.info("User {} authenticated with role: {}", username, role.getName());
        }

      
        return new UsernamePasswordAuthenticationToken(
                username,
                password,
                authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // Hỗ trợ UsernamePasswordAuthenticationToken
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
