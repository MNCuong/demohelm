package com.example.web_monitor.aspecttest;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.aspect.ActionLogAspect;
import com.example.web_monitor.model.enums.ActionDetailKey;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.repository.ActionRepository;
import com.example.web_monitor.repository.UserRepository;
import com.example.web_monitor.service.ActionLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionLogAspectTest {

    @Mock
    private ActionLogService actionLogService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private TrackAction trackAction;

    @Mock
    private CodeSignature codeSignature;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActionLogAspect actionLogAspect;

    @Test
    void logUserAction_ShouldExtractParams_AndCallService() throws Exception {
        // --- 1. GIVEN ---
        // SỬA: Đổi tên tham số thành "username" và "id" để khớp với Enum USERNAME, USER_ID
        String[] paramNames = new String[]{"username", "id"};
        Object[] paramValues = new Object[]{"admin", 123L};

        lenient().when(trackAction.value()).thenReturn(ActionType.CHECK_CONNECTION); // Sửa khớp với log lỗi

        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(codeSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(paramValues);

        // Mock Jackson: Lưu ý Jackson sẽ thêm dấu ngoặc kép vào chuỗi
        when(objectMapper.writeValueAsString("admin")).thenReturn("\"admin\"");
        when(objectMapper.writeValueAsString(123L)).thenReturn("123");

        // --- 2. WHEN ---
        actionLogAspect.logUserAction(joinPoint, trackAction);

        // --- 3. THEN ---
        verify(actionLogService, times(1)).logAction(
                eq(ActionType.CHECK_CONNECTION),
                argThat(map -> {
                    // SỬA: Kiểm tra đúng key USERNAME và USER_ID
                    boolean hasUser = map.containsKey(ActionDetailKey.USERNAME)
                            // Giá trị trong map là JSON string nên có dấu ngoặc kép
                            && map.get(ActionDetailKey.USERNAME).equals("\"admin\"");

                    boolean hasId = map.containsKey(ActionDetailKey.USER_ID)
                            && map.get(ActionDetailKey.USER_ID).equals("123");

                    return hasUser && hasId;
                })
        );
    }

    @Test
    void logUserAction_ShouldHandleNullParams() {
        // Test trường hợp hàm không có tham số nào
        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(codeSignature.getParameterNames()).thenReturn(new String[]{});
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        lenient().when(trackAction.value()).thenReturn(ActionType.SAVE_USER);

        actionLogAspect.logUserAction(joinPoint, trackAction);

        // Verify map rỗng được truyền đi
        verify(actionLogService).logAction(eq(ActionType.SAVE_USER), argThat(Map::isEmpty));
    }

    @Test
    void logUserAction_ShouldMapParamsToEnum_AndCallService() throws Exception {
        // "userDto" -> sẽ map thành REQUEST_BODY
        // "id" -> sẽ map thành USER_ID
        String[] paramNames = new String[]{"userDto", "id"};
        Object[] paramValues = new Object[]{"dummyData", 123L};

        lenient().when(trackAction.value()).thenReturn(ActionType.SAVE_USER);

        // Mock CodeSignature
        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(codeSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(paramValues);

        // Mock Jackson
        when(objectMapper.writeValueAsString("dummyData")).thenReturn("\"dummyData\"");
        when(objectMapper.writeValueAsString(123L)).thenReturn("123");

        // --- 2. WHEN ---
        actionLogAspect.logUserAction(joinPoint, trackAction);

        // --- 3. THEN ---
        // Kiểm tra Service được gọi với Map có Key là ENUM
        verify(actionLogService, times(1)).logAction(
                eq(ActionType.SAVE_USER),
                argThat(map -> {
                    // Check key REQUEST_BODY (do "userDto" map sang)
                    boolean hasBody = map.containsKey(ActionDetailKey.REQUEST_BODY);
                    boolean hasId = map.containsKey(ActionDetailKey.USER_ID);

                    return hasBody && hasId;
                })
        );
    }
}