package com.example.web_monitor.annotation;

import com.example.web_monitor.model.enums.ActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Chỉ dùng cho method
@Retention(RetentionPolicy.RUNTIME) // Tồn tại lúc chạy
public @interface TrackAction {
    ActionType value(); // Bắt buộc phải truyền loại hành động (VD: LOGIN, CREATE_POST)
}