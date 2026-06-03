package com.example.web_monitor.model.enums;

import lombok.Getter;

@Getter
public enum ActionDetailKey {
    // 1. Các Key chuẩn (Phải khớp với Test)
    USERNAME("Tên tài khoản"),
    USER_ID("ID người dùng"),
    IP_ADDRESS("Địa chỉ IP"),
    REQUEST_BODY("Dữ liệu gửi lên"),
    OLD_VALUE("Giá trị cũ"),
    NEW_VALUE("Giá trị mới"),
    UNKNOWN("Không xác định"); // Key dự phòng

    private final String description;

    ActionDetailKey(String description) {
        this.description = description;
    }

    // 2. Logic Map tên biến sang Enum
    public static ActionDetailKey fromString(String paramName) {
        if (paramName == null) return UNKNOWN;
        String upperName = paramName.toUpperCase();

        try {
            // A. Thử map chính xác (VD: "USERNAME" -> USERNAME)
            return ActionDetailKey.valueOf(upperName);
        } catch (IllegalArgumentException e) {
            // B. Map tương đối (Quan trọng cho Test)

            // "userDto", "postDto", "body" -> REQUEST_BODY
            if (upperName.contains("DTO") || upperName.contains("BODY")) return REQUEST_BODY;

            // "clientIp", "ipAddress" -> IP_ADDRESS
            if (upperName.contains("IP")) return IP_ADDRESS;

            // "id", "userId" -> USER_ID
            if (upperName.equals("ID") || upperName.equals("USERID")) return USER_ID;

            // "username" -> USERNAME (Đề phòng trường hợp valueOf không bắt được nếu bạn đặt tên khác)
            if (upperName.contains("USERNAME") || upperName.contains("USER_NAME")) return USERNAME;

            return UNKNOWN;
        }
    }
}