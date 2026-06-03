package com.example.web_monitor.aspect;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.model.enums.ActionDetailKey;
import com.example.web_monitor.service.ActionLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Aspect
@Component
@Slf4j
public class ActionLogAspect {

    private final ActionLogService actionLogService;
    private final ObjectMapper objectMapper;
    private static final Logger log = getLogger(lookup().lookupClass());

    public ActionLogAspect(ActionLogService actionLogService, ObjectMapper objectMapper) {
        this.actionLogService = actionLogService;
        this.objectMapper = objectMapper;
    }

    // Pointcut: Chạy sau khi method được đánh dấu @TrackAction thực hiện thành công
    @AfterReturning(pointcut = "@annotation(trackAction)")
    public void logUserAction(JoinPoint joinPoint, TrackAction trackAction) {
        // Lấy value từ annotation (VD: ActionType.create_post)
        log.info("Catch action to log: {}", trackAction.value());

        try {
            // 1. Lấy tham số
            Map<ActionDetailKey, String> params = getMethodParams(joinPoint);

            // 3. Gọi Service lưu
            actionLogService.logAction(trackAction.value(), params);
            log.info("Tracked action logged successfully: {}", trackAction.value());
        } catch (Exception e) {
            log.info("Error logging tracked action: {}", trackAction.value());
            e.printStackTrace(); // In lỗi ra xem tại sao
        }
    }

    // Helper: Trích xuất tên biến và giá trị thành Map
    private Map<ActionDetailKey, String> getMethodParams(JoinPoint joinPoint) {
        Map<ActionDetailKey, String> params = new HashMap<>();

        // Lấy tên các tham số (VD: ["username", "email"])
        String[] parameterNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        // Lấy giá trị các tham số (VD: ["admin", "admin@gmail.com"])
        Object[] args = joinPoint.getArgs();

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                Object argValue = args[i];

                // 1. BỎ QUA CÁC THAM SỐ KỸ THUẬT (Filter Noise)
                if (argValue instanceof BindingResult
                        || argValue instanceof Model
                        || argValue instanceof HttpServletRequest
                        || argValue instanceof HttpServletResponse) {
                    continue;
                }

                String key = parameterNames[i];
                Object valueObj = args[i];
                ActionDetailKey keyEnum = ActionDetailKey.fromString(key);
                String valueStr;

                // Nếu map ra UNKNOWN -> Bỏ qua luôn (không put vào map)
                if (keyEnum == ActionDetailKey.UNKNOWN) {
                    continue;
                }

                try {
                    // Nếu là object phức tạp -> Convert JSON string
                    // Nếu là simple type (int, string) -> toString()
                    valueStr = objectMapper.writeValueAsString(valueObj);
                } catch (Exception e) {
                    valueStr = String.valueOf(valueObj);
                }

                params.put(keyEnum, valueStr);
            }
        }
        return params;
    }
}