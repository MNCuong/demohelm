package com.example.web_monitor.model.enums;

public enum ActionType {
    UPDATE_PROFILE("User updated profile"),
    CHECK_CONNECTION("Checked participant connection"),
    SAVE_USER("Save user"),
    EDIT_USER("Edit user"),
    DELETE_USER("Delete user"),
    DISABLE_USER("Disable user"),
    ENABLE_USER("Enable user"),
    SAVE_PARTICIPANT("Save participant"),
    EDIT_PARTICIPANT("Edit participant"),
    DELETE_PARTICIPANT("Delete participant"),
    RESEND("Resend electric"),
    KICK_PARTICIPANT("Kick participant"),

    ;
    // Thêm các hành động khác tại đây

    ActionType(String message) {
        this.message = message;
    }

    private final String message;
}