package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.MenuEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
       List<MenuEntity> findAllByIsActiveOrderByLvelAscDisplayOrderAsc(String isActive);

       /**
        * Query tùy chỉnh để lấy tất cả menu active DỰA TRÊN danh sách role của user.
        * Dùng JOIN FETCH roles để tránh N+1 (tùy chọn) và DISTINCT để loại trùng lặp.
        */
       @Query("SELECT DISTINCT m FROM MenuEntity m " +
              "JOIN m.roles r " +
              "WHERE m.isActive = :isActive " +
              "AND r.name IN :roleNames " +
              "ORDER BY m.lvel, m.displayOrder")
       List<MenuEntity> findActiveMenusByRole(
              @Param("isActive") String isActive,
              @Param("roleNames") Collection<String> roleNames
       );
}