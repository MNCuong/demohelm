package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    //@EntityGraph(attributePaths = "role")
    UserEntity findByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.role WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithRoles(@Param("username") String username);

    @Query(value = "SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.role r",
            countQuery = "SELECT count(DISTINCT u) FROM UserEntity u")
    Page<UserEntity> findAllWithRoles(Pageable pageable);

    // Sửa 'u.roles' thành 'u.role' (hoặc tên chính xác trong Entity của bạn)
    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.role r WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    Page<UserEntity> searchUsersByEnabled(boolean enabled, Pageable pageable);

    void deleteByUsername(String username);
}
