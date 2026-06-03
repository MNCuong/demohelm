package com.example.web_monitor.servicetest;

import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.model.enums.ActionDetailKey;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.repository.ActionRepository;
import com.example.web_monitor.repository.UserRepository;
import com.example.web_monitor.service.ActionLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic; // Cần thiết để mock SecurityContext
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionLogServiceTest {

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActionLogService actionLogService;

    // Biến để mock static SecurityContextHolder
    private MockedStatic<SecurityContextHolder> mockedSecurity;

    @BeforeEach
    void setUp() {
        // Mở mock static trước mỗi test
        mockedSecurity = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        // Đóng mock static sau khi test xong để không ảnh hưởng test khác
        mockedSecurity.close();
    }

    @Test
    void logAction_ShouldSaveEntity_WithEnumDetails() {
        // --- 1. GIVEN (Giả lập dữ liệu) ---
        Map<ActionDetailKey, String> params = Map.of(
                ActionDetailKey.USERNAME, "admin",
                ActionDetailKey.IP_ADDRESS, "127.0.0.1"
        );

        // Giả lập Security Context để lấy username
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");

        // Mock hành vi static của SecurityContextHolder
        mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        // Giả lập tìm thấy User trong DB
        UserEntity mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        // --- 2. WHEN (Chạy hàm thật) ---
        // Vì actionLogService được @InjectMocks, code thật sẽ chạy
        actionLogService.logAction(ActionType.UPDATE_PROFILE, params);

        // 3. THEN
        verify(actionRepository).save(argThat(entity -> {
            // Debug: In ra để xem nếu test fail
            System.out.println("DEBUG Entity Type: " + entity.getActionType());
            System.out.println("DEBUG Entity Details: " + entity.getDetails().size());

            // Check 1: Action Type
            boolean typeOk = entity.getActionType() == ActionType.UPDATE_PROFILE;

            // Check 2: List details phải có dữ liệu
            if (entity.getDetails() == null || entity.getDetails().isEmpty()) return false;

            // Check 3: Nội dung chi tiết
            boolean hasIp = entity.getDetails().stream()
                    .anyMatch(d -> d.getDetailKey() == ActionDetailKey.IP_ADDRESS
                            && "127.0.0.1".equals(d.getDetailValue()));

            return typeOk && hasIp;
        }));
    }
}