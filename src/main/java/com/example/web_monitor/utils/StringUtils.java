package com.example.web_monitor.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class StringUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private StringUtils(){
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    public static String convertArgsToString(Object[] args) {
        if (args == null || args.length == 0) return "No args";
        try {
            // Lưu ý: Cẩn thận với các object quá lớn hoặc chứa thông tin nhạy cảm (password)
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "Could not serialize args";
        }
    }
}
