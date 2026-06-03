package com.example.web_monitor.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParticipantDto {

    private Long id;

    @NotBlank(message = "VSDCODE không được để trống")
    @Pattern(regexp = "^[0-9]{3}$", message = "VSDCODE phải gồm đúng 3 chữ số")
    private String vsdcode;

    @Pattern(
            regexp = "^(?=.{8}$)VSDC[A-Za-z0-9]*X*$",
            message = "BICCODE phải có dạng VSDC + 3 ký tự mã thành viên của thành viên + ký tự X cho đủ 8 ký tự"
    )
    private String biccode;


    @Size(max = 5, message = "Tên viết tắt không được vượt quá 5 ký tự")
    private String shortname;

    @NotBlank(message = "Tên TVLK tiếng Anh không được để trống")
    private String fullnameen;

    @NotBlank(message = "Tên TVLK tiếng Việt không được để trống")
    private String fullnamevn;

//    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

//    @NotNull(message = "Nhóm không được để trống")
    private String groupid;

    private Long userid;
    private String usrname;
    private LocalDateTime updatetime;
    private Integer consumers;
    private String groupqueue;
    private String usertype;
    private String ipAddr;
}
