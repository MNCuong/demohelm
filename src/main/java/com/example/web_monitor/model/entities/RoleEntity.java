package com.example.web_monitor.model.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "APP_ROLES")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq_gen")
    @SequenceGenerator(
            name = "role_seq_gen",
            sequenceName = "APP_ROLES_SEQ", // Tên sequence trong DB
            allocationSize = 50 // Phải khớp với "INCREMENT BY 50"
    )
    private Long id;

    @Column(length = 20, unique = true, nullable = false)
    private String name; // Ví dụ: "ROLE_ADMIN"

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "APP_ROLE_MENUS", // Tên bảng trung gian (join table)
        joinColumns = @JoinColumn(name = "role_id"),      // Khóa ngoại trỏ đến bảng APP_ROLES
        inverseJoinColumns = @JoinColumn(name = "menu_id") // Khóa ngoại trỏ đến bảng MENU
    )
    private Set<MenuEntity> menus = new HashSet<>();


    @OneToMany(
        mappedBy = "role", 
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL, // Tùy chọn: xóa role thì xóa user (ít dùng)
        orphanRemoval = false // Tùy chọn
    )
    private Set<UserEntity> users = new HashSet<>();
}
