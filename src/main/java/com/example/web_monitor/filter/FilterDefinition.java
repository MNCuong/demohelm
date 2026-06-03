package com.example.web_monitor.filter;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class FilterDefinition {
    private String field;       // Tên param (VD: "sender", "content")
    private String label;       // Nhãn hiển thị (VD: "Người gửi")
    private Type type;          // Loại input
    private List<Option> options; // Cho dropdown

    public enum Type { TEXT, DATE_RANGE, SELECT }

    @Data @Builder
    public static class Option {
        private String value;
        private String label;
    }
}
