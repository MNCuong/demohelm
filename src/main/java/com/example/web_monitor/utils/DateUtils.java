package com.example.web_monitor.utils;

import java.time.format.DateTimeFormatterBuilder;

public class DateUtils {
    public static String formatDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toString();
        }
        // Check java.sql.Date BEFORE java.util.Date since java.sql.Date extends java.util.Date
        // java.sql.Date throws UnsupportedOperationException on toInstant()
        // Convert to Timestamp first to preserve time information from milliseconds
        if (value instanceof java.sql.Date sqlDate) {
            java.sql.Timestamp timestamp = new java.sql.Timestamp(sqlDate.getTime());
            return timestamp.toLocalDateTime()
                    .format(new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd HH:mm:ss")
                            .toFormatter());
        }
        if (value instanceof java.util.Date date) {
            return new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .toFormatter()
                    .format(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        return value.toString();
    }
}
