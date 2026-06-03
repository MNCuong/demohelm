package com.example.web_monitor.configuration;

import com.example.web_monitor.model.entities.RoleEntity;
import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.repository.RoleRepository;
import com.example.web_monitor.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, 
                           RoleRepository roleRepository, 
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        //Tạo Roles nếu chưa có
        RoleEntity adminRole = createRoleIfNotFound("ROLE_ADMIN");

        //Tạo User Admin nếu chưa có
        if (userRepository.findByUsername("admin.msp") == null) {
            
            UserEntity adminUser = new UserEntity();
            adminUser.setUsername("admin.msp");
            adminUser.setPassword(passwordEncoder.encode("123@abcd"));
            
            adminUser.setEmail("admin.msp@example.com");
            adminUser.setNotes("Default Admin MSP User");
            adminUser.setEnabled(true);

            // Gán role cho user
            adminUser.setRole(adminRole);
            
            userRepository.save(adminUser);
            System.out.println(">>> Đã tạo user ADMIN MSP mặc định <<<");
        }
    }

    private RoleEntity createRoleIfNotFound(String name) {
        RoleEntity role = roleRepository.findByName(name);
        
        if (role == null) {
            role = new RoleEntity();
            role.setName(name);
            role = roleRepository.save(role); // Lưu role mới và gán lại
        }
        return role;
    }
}