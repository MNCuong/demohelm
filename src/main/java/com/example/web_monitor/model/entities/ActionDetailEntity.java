package com.example.web_monitor.model.entities;

import com.example.web_monitor.model.enums.ActionDetailKey;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "action_details")
public class ActionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // <-- Bắt buộc phải là STRING
    @Column(name = "detail_key", length = 50)
    private ActionDetailKey detailKey;

    // Giá trị thực tế (VD: "admin", "192.168.1.1")
    // Lưu Text để chứa được nhiều loại dữ liệu
    @Column(name = "detail_value", length = 2048)
    private String detailValue;

    // (Optional) Mô tả thêm nếu cần
    @Column(name = "description")
    private String description;

    // Quan hệ Many-to-One về bảng Action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionEntity action;
}
