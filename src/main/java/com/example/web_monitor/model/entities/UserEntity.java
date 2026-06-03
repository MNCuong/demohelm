package com.example.web_monitor.model.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "APP_USERS", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(
            name = "user_seq_gen",
            sequenceName = "APP_USERS_SEQ", // Tên sequence trong DB
            allocationSize = 50 // Phải khớp với "INCREMENT BY 50"
    )
    private Long id;

    @Column(length = 128, nullable = false, unique = true)
    private String username;

    @Column(length = 64, nullable = false)
    private String password; // Sẽ được mã hóa

    @Column(length = 128, nullable = false)
    private String email;

    @Column(length = 128)
    private String notes;

    private boolean enabled; // User có hoạt động không?

    // Mối quan hệ Many-to-Many
    // Một user có nhiều role, một role có nhiều user
    // @ManyToMany(fetch = FetchType.LAZY)
    // @JoinTable(
    //     name = "APP_USERS_ROLES",
    //     joinColumns = @JoinColumn(name = "user_id"),
    //     inverseJoinColumns = @JoinColumn(name = "role_id")
    // )
    // private Set<RoleEntity> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID") // Tên cột khóa ngoại mới trong bảng APP_USERS
    private RoleEntity role;
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<ActionEntity> actions = new ArrayList<>();

}
