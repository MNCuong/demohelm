package com.example.web_monitor.utils;

public final class ObjectUtils {

    // Ngăn không cho khởi tạo class
    private ObjectUtils() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /**@param val object cần kiểm tra
     * @return true nếu hợp lệ, false nếu null hoặc empty*/
    public static boolean isValid(Object val) {
        return val != null && !val.toString().trim().isEmpty();
    }
}

