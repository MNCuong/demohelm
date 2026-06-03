package com.example.web_monitor.service;

import static com.example.web_monitor.utils.RoleUtils.*;

import com.example.web_monitor.model.entities.RoleEntity;
import com.example.web_monitor.model.entities.UserEntity;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.web_monitor.dto.UserDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.mapper.UserMapper;
import com.example.web_monitor.repository.RoleRepository;
import com.example.web_monitor.repository.UserRepository;

import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    private static final Logger log = getLogger(lookup().lookupClass());

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void saveUser(UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getPassword() == null || userDto.getEmail() == null) {
            throw new BusinessException(ErrorCode.MISSING_INFOMATION);
        }
        if (userRepository.findByUsername(userDto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTED);
        }
        UserEntity newUser = userMapper.toEntity(userDto);
        //Mã hóa password
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setEnabled(true);

        //Xử lý Role
        RoleEntity userRole = roleRepository.findByName("ROLE_" + userDto.getRole());
        if (userRole == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_EXISTED);
        }

        newUser.setRole(userRole);
        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAllUsers(int pageNo, int pageSize, String sortField, String sortDir, String keyword, String lang) {

        String dbSortField = sortField;
        log.info("sortField: {}", sortField);
        if ("role".equals(sortField)) {
            //dbSortField = "r.name";
            dbSortField = "role.name";
        }
        Sort sort = Sort.by(dbSortField);
        if ("desc".equals(sortDir)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<UserEntity> pageEntity;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String keyWordTemp = keyword.trim();

            if (keyWordTemp.equalsIgnoreCase("Active") || keyWordTemp.equalsIgnoreCase("Inactive")) {
                boolean status = keyWordTemp.equalsIgnoreCase("Active");
                pageEntity = userRepository.searchUsersByEnabled(status, pageable);
            } else {
                if ("vi".equalsIgnoreCase(lang)) {
                    pageEntity = userRepository.searchUsers(
                            parseRoleKeyword(keyWordTemp), pageable);
                } else {
                    pageEntity = userRepository.searchUsers(keyWordTemp, pageable);
                }
            }
        } else {
            pageEntity = userRepository.findAllWithRoles(pageable);
        }
        return pageEntity.map(userMapper::toDto);
    }

    public UserDto findUserByUserName(String userName) {
        UserEntity user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTED);
        }
        return userMapper.toDto(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void disableUser(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTED);
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void editUser(UserDto userDto) {
        UserEntity user = userRepository.findByUsername(userDto.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTED);
        }
        user.setNotes(userDto.getNotes());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (userDto.getPassword().length() < 8 || !userDto.getPassword().matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).*$")) {
                throw new BusinessException(ErrorCode.PASSWORD_INVALID);
            }
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        if (userDto.getRole() != null) {
            RoleEntity role = roleRepository.findByName("ROLE_" + userDto.getRole());
            if (role == null) {
                throw new BusinessException(ErrorCode.ROLE_NOT_EXISTED);
            }
            user.setRole(role);
        }
        userRepository.save(user);
    }


    public UserDto findByUsername(String username) {
        return userMapper.toDto(userRepository.findByUsername(username));
    }

    public boolean updateProfile(UserDto userDto) {
        try {
            UserEntity existingUser = userRepository.findByUsername(userDto.getUsername());

            if (existingUser == null) {
                return false;
            }

            existingUser.setEmail(userDto.getEmail());
            existingUser.setNotes(userDto.getNotes());

            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }

            userRepository.save(existingUser);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEnabled(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTED))
                .isEnabled();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteByUsername(String username) {
        try {
            userRepository.deleteByUsername(username);
        } catch (BusinessException e) {
            throw new BusinessException(e.getErrorCode());
        }
    }

    private String parseRoleKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return keyword;

        String k = normalize(keyword);
        if (k.contains(KW_QUAN_TRI)) {
            return ROLE_ADMIN;
        }
        if (k.contains(KW_GIAM_SAT)) {
            return ROLE_MONITOR;
        }
        if (k.contains(KW_VAN_HANH)) {
            return ROLE_OPERATOR;
        }
        return keyword;
    }

    private String normalize(String input) {
        if (input == null) return null;
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern
                .compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(temp)
                .replaceAll("")
                .toLowerCase()
                .trim();
    }
}