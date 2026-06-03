package com.example.web_monitor.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 20, message = "Tên đăng nhập tối đa 20 ký tự")
    private String username;


    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải tối thiểu 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Mật khẩu phải chứa chữ, số và ký tự đặc biệt"
    )
    private String password;

    private String role;
    private String notes;
    private boolean enabled;
    private String avatarUrl;
}
