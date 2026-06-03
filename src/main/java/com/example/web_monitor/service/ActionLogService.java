package com.example.web_monitor.service;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.model.entities.ActionEntity;
import com.example.web_monitor.model.enums.ActionDetailKey;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.model.entities.UserEntity;
import com.example.web_monitor.repository.ActionRepository;
import com.example.web_monitor.repository.UserRepository;
import com.example.web_monitor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ActionLogService {

    private final ActionRepository actionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ActionLogService(ActionRepository actionRepository, UserRepository userRepository, UserService userService) {
        this.actionRepository = actionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public void logAction(ActionType type, Map<ActionDetailKey, String> params) {
        // Lấy username từ Spring Security Context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username);
        if (user==null) throw new BusinessException(ErrorCode.USER_NOT_EXISTED);

        // Tạo Entity Action
        ActionEntity actionLog = ActionEntity.builder()
                .actionType(type)
                .user(user)// Lưu details vào entity
                .build();

        // Loop qua Map params để tạo Action Detail con
        if (params != null) {
            params.forEach((key, value) -> {
                // Gọi helper method đã viết trong Entity
                if (key != ActionDetailKey.UNKNOWN) actionLog.addDetail(key, value, key.getDescription());
            });
        }

        actionRepository.save(actionLog);
    }
    @TrackAction(ActionType.DISABLE_USER)
    public void disableUserAction(String username) {
        userService.disableUser(username);
    }

    @TrackAction(ActionType.ENABLE_USER)
    public void enableUserAction(String username) {
        userService.disableUser(username);
    }

}