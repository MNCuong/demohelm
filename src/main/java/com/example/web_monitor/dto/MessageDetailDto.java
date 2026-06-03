package com.example.web_monitor.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Builder
@Data
public class MessageDetailDto {
    private String orgSeqId;
    private String status;
    private LocalDateTime txDate;

}
