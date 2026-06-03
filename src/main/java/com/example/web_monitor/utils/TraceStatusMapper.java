package com.example.web_monitor.utils;

public class TraceStatusMapper {

    public static String mapSentStatusText(String status) {
        if ("N".equalsIgnoreCase(status)) return "Đang gửi TV";
        if ("S".equalsIgnoreCase(status)) return "Đã gửi TV";
        return status;
    }

    /**
     * Mapping cho bảng TXMSGLOG_DETAIL (chỉ còn các trạng thái P, E, N, S).
     */
    public static String mapDetailStatusCode(String status) {
        if ("E".equalsIgnoreCase(status)) {
            return "X";
        }
        return "V";
    }

    public static String mapDetailStatusText(String status) {
        if ("P".equalsIgnoreCase(status)) return "Đã gửi sang IG";
        if ("E".equalsIgnoreCase(status)) return "Sai format Swift";
        return mapSentStatusText(status);
    }

    public static String mapMsgTypeByStatus(String status) {
        if ("P".equalsIgnoreCase(status) || "E".equalsIgnoreCase(status)) {
            return "TMX"; // trạng thái gửi/nhận tại IG
        }
        if ("N".equalsIgnoreCase(status) || "S".equalsIgnoreCase(status)) {
            return "SEN"; // trạng thái gửi xuống TV
        }
        return "UNK";
    }
}

