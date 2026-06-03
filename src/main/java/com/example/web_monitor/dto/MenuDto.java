package com.example.web_monitor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Data Transfer Object (DTO) cho MenuEntity.
 * Dùng để truyền tải dữ liệu menu giữa các tầng (đặc biệt là ra API)
 * mà không lộ chi tiết của Entity và tránh lỗi đệ quy.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "children") // Tránh in đệ quy quá sâu khi logging
@EqualsAndHashCode(of = "id") // So sánh DTO dựa trên ID
@JsonInclude(JsonInclude.Include.NON_NULL) // Không serialize trường null ra JSON
public class MenuDto {

    private Long id;

    private String objectName;

    private String name;

    private Integer lvel;

    private String isLast;

    private String isActive;

    private Integer displayOrder;

    private Long parentId;

    @Builder.Default
    private Set<MenuDto> children = new LinkedHashSet<>();

}