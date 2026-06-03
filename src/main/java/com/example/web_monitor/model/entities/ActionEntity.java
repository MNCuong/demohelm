package com.example.web_monitor.model.entities;

import com.example.web_monitor.model.enums.ActionDetailKey;
import com.example.web_monitor.model.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Pattern này rất tiện lợi khi khởi tạo object
@Entity
@Table(name = "user_actions") // Tránh dùng tên "Action" (từ khóa SQL)
@EntityListeners(AuditingEntityListener.class) // Kích hoạt tính năng tự động ghi thời gian
public class ActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Khuyên dùng IDENTITY cho MySQL/Postgres thay vì AUTO
    private Long id;

    // 1. Mapping Enum
    @Enumerated(EnumType.STRING) // Lưu dưới dạng String (VD: "LOGIN") để dễ đọc trong DB
    @Column(name = "action_type", columnDefinition = "VARCHAR2(50)")
    private ActionType actionType;

    // 2. Mapping User (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY) // Lazy load để tránh query dư thừa khi không cần thiết
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại trỏ về bảng User
    private UserEntity user;

    // 3. Thời gian thực hiện (Tự động)
    @CreatedDate
    @Column(nullable = false, updatable = false) // Không cho phép sửa thời gian đã ghi
    private LocalDateTime timestamp;

    // 1. Bỏ field "details" string cũ đi
    // 2. Thêm list ActionDetailEntity
    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Để Builder không set null list này
    private List<ActionDetailEntity> details = new ArrayList<>();

    // Helper method để thêm detail dễ dàng (quan trọng cho JPA)
    public void addDetail(ActionDetailKey key, String value, String desc) {
        ActionDetailEntity detail = ActionDetailEntity.builder()
                .detailKey(key)
                .detailValue(value)
                .description(desc)
                .action(this) // Set ngược lại cha
                .build();
        this.details.add(detail);
    }
}