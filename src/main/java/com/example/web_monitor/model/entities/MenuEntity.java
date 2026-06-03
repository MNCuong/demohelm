package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet; 

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

/**
 * Entity này ánh xạ tới bảng MENU trong cơ sở dữ liệu,
 * dùng để lưu trữ cấu trúc menu (bao gồm cả menu con).
 */

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"parent", "children"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "MENU")
public class MenuEntity {

    public static final String IS_ACTIVE = "Y";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_seq_gen")
    @SequenceGenerator(
            name = "menu_seq_gen",   
            sequenceName = "MENU_SEQ", 
            allocationSize = 1 
    )
    private Long id;

     @Column(name = "OBJECT_NAME", nullable = false, length = 100)
    private String objectName;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "LVEL", nullable = false, length = 10)
    private Integer lvel; 

    @Column(name = "IS_LAST", nullable = false, length = 1)
    private String isLast;

    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    private String isActive; 

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    // --- Quan hệ Cha-Con (Parent-Child) ---

    /**
     * Mối quan hệ Nhiều-Một (Many-to-One):
     * Nhiều menu con có thể trỏ đến MỘT menu cha.
     * fetch = FetchType.LAZY: Chỉ tải thông tin 'parent' khi được gọi,
     * tránh truy vấn thừa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID") // Tên cột khóa ngoại trong bảng APP_MENU
    private MenuEntity parent;

    /**
     * Mối quan hệ Một-Nhiều (One-to-Many):
     * MỘT menu cha có thể có NHIỀU menu con.
     * mappedBy = "parent": Chỉ ra rằng mối quan hệ này được quản lý
     * bởi trường 'parent' ở phía 'ManyToOne' (tức là Menu con).
     *
     * Khi tải một menu, hãy tải LUÔN danh sách con của nó.
     * Điều này hữu ích vì chúng ta cần menu con ngay lập tức.
     *
     * cascade = CascadeType.ALL: (Tùy chọn) Nếu xóa cha thì xóa luôn con.
     * orphanRemoval = true: (Tùy chọn) Nếu xóa con khỏi danh sách thì xóa luôn trong DB.
     */
    @OneToMany(
            mappedBy = "parent",
            fetch = FetchType.LAZY, // Tải 'con' ngay khi tải 'cha'
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC") // LUÔN sắp xếp danh sách con theo thứ tự
    private Set<MenuEntity> children = new LinkedHashSet<>(); // Dùng LinkedHashSet để giữ thứ tự


    @ManyToMany(mappedBy = "menus", fetch = FetchType.LAZY)
    private Set<RoleEntity> roles = new HashSet<>();
}