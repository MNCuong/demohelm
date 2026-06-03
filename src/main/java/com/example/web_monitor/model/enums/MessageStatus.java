package com.example.web_monitor.model.enums;

import lombok.Getter;

@Getter
public enum MessageStatus {
    // Định nghĩa các trạng thái (Value trong DB, Label hiển thị UI)
    E("E", "Evaluated"),
    N("N", "New"),
    P("P", "Pending"),
    ;

    private final String value;
    private final String label;

    MessageStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
