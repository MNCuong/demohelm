package com.example.web_monitor.filter.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp; // [CẦN THIẾT] Để convert Date
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericSpecification {

    public static <T> Specification<T> create(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((k, v) -> {
                if (v == null || v.toString().trim().isEmpty()) return;
                String value = v.toString();

                // [SỬA 1]: Đổi logic bắt suffix từ "_from" sang "From" (CamelCase)
                if (k.endsWith("From")) {
                    String field = k.substring(0, k.length() - 4); // Cắt bỏ "From" -> lấy "txDate"
                    try {
                        // Parse yyyy-MM-dd -> Timestamp đầu ngày
                        LocalDate date = LocalDate.parse(value);
                        predicates.add(cb.greaterThanOrEqualTo(root.get(field), Timestamp.valueOf(date.atStartOfDay())));
                    } catch (Exception e) {
                        // Bỏ qua nếu parse lỗi
                    }
                }
                // [SỬA 2]: Đổi "_to" sang "To"
                else if (k.endsWith("To")) {
                    String field = k.substring(0, k.length() - 2); // Cắt bỏ "To"
                    try {
                        LocalDate date = LocalDate.parse(value);
                        // Convert sang cuối ngày (23:59:59)
                        predicates.add(cb.lessThanOrEqualTo(root.get(field), Timestamp.valueOf(date.atTime(LocalTime.MAX))));
                    } catch (Exception e) {
                        // Bỏ qua
                    }
                }
                // [SỬA 3]: Xử lý các trường còn lại (Select/Text)
                else {
                    try {
                        // Kiểm tra an toàn: Nếu field không tồn tại, dòng này sẽ throw IllegalArgumentException
                        Path<Object> path = root.get(k);

                        if (path.getJavaType() == String.class) {
                            predicates.add(cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%"));
                        } else {
                            predicates.add(cb.equal(path.as(String.class), value));
                        }
                    } catch (IllegalArgumentException e) {
                        // [QUAN TRỌNG]: Bắt lỗi "Could not resolve attribute" tại đây và bỏ qua nó.
                        // Điều này ngăn chặn hệ thống bị crash khi gặp tham số rác hoặc tham số không map với Entity.
                    }
                }
            });
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}