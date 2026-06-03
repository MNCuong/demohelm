package com.example.web_monitor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TraceMsgLogDto {
    private String autoId;
    private String orgSeqId;
    private String actionTime;   // đã format sẵn
    private String stpRefId;
    private String bicCode;
    private String statusCode;   // V / X để hiển thị màu
    private String statusText;   // mô tả trạng thái
    private String msgType;      // TMX / SEN / SIG
    private String fileName;
    private String queueName;
    private String isResend;
}

