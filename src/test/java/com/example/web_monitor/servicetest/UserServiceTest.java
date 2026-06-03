package com.example.web_monitor.servicetest;

import com.example.web_monitor.dto.UserDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.mapper.UserMapper;
import com.example.web_monitor.model.entities.RoleEntity;
import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.repository.RoleRepository;
import com.example.web_monitor.repository.UserRepository;
import com.example.web_monitor.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;
    private UserEntity userEntity;
    private RoleEntity roleEntity;


    @BeforeEach
    void init() {
        // Chuẩn bị dữ liệu đầu vào chuẩn (Happy Case data)
        userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setPassword("rawPassword");
        userDto.setRole("ADMIN");
        userDto.setEmail("test@example.com");

        //Chuẩn bị Entity tương ứng
        userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setEmail("test@example.com");

        //Chuẩn bị Role
        roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setName("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Save User - Success")
    void saveUser_Success() {

        // Giả lập: khi gọi thì trả về null để thể hiện user chưa tồn tại
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(null);
        
        // Giả lập: khi gọi thì trả về entity đã init ở trên
        when(userMapper.toEntity(userDto)).thenReturn(userEntity);
        
        // Giả lập: Encode pass
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        
        // Giả lập: khi gọi thì trả về role Entity đã init ở trên
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(roleEntity);

        // Call
        userService.saveUser(userDto);

        // --- ASSERT ---
        // Kiểm tra password đã mã hóa được set vào entity chưa
        assertEquals("encodedPassword", userEntity.getPassword());
        
        // Kiểm tra Role đã gán đúng chưa
        assertEquals(roleEntity, userEntity.getRole());
        
        // Kiểm tra xem trạng thái enabled có true không
        assertTrue(userEntity.isEnabled());

        // Verify hàm save được gọi 1 lần
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    @DisplayName("Save User - Fail: User Existed")
    void saveUser_Fail_UserExists() {
        // --- ARRANGE ---
        // Giả lập: User ĐÃ tồn tại (trả về 1 entity bất kỳ thay vì null)
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(userEntity);

        // --- ACT & ASSERT ---
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.saveUser(userDto);
        });

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());

        // Đảm bảo không bao giờ gọi lệnh save
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Save User - Fail: Role Not Found")
    void saveUser_Fail_RoleNotFound() {
        // --- ARRANGE ---
        // Setup tình huống role không tồn tại
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(null);
        when(userMapper.toEntity(userDto)).thenReturn(userEntity);
        
        // Giả lập: Tìm Role trả về null
        when(roleRepository.findByName("ROLE_" + userDto.getRole())).thenReturn(null);

        // --- ACT & ASSERT ---
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.saveUser(userDto);
        });

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Save User - Fail: Email Existed")
    void saveUser_Fail_EmailExists() {
        // --- ARRANGE ---
        //Giả lập Username chưa tồn tại
        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(null);

        //Giả lập Email ĐÃ tồn tại (trả về true)
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        // --- ACT & ASSERT ---
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.saveUser(userDto);
        });

        // Kiểm tra đúng mã lỗi EMAIL_EXISTED
        assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());

        // Đảm bảo không bao giờ gọi lệnh save
        verify(userRepository, never()).save(any());
    }
}