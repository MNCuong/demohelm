package com.example.web_monitor.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Slf4j
public final class CsvUtils {

    private CsvUtils() {
    }

    public static final String[] HEADERS = {
            "SendTime", "RecvTime", "MsgType", "SenderBIC",
            "ReceiverBIC", "Status", "MsgRef", "RelatedRef", "Summary",
    };

    public static Function<Object[], String[]> rowMapper() {
        return row -> {
            String[] result = new String[9];

            try {
                // sendTime - index 0
                result[0] = formatDateTime(row[0]);

                // recvTime - index 1
                result[1] = formatDateTime(row[1]);

                // msgType - index 2
                result[2] = row[2] != null ? row[2].toString().trim() : "";

                // senderBic - index 3
                result[3] = row[3] != null ? row[3].toString() : "";

                // receiverBic - index 4
                result[4] = row[4] != null ? row[4].toString() : "";

                // status - index 5
                result[5] = row[5] != null ? row[5].toString() : "NAK";

                // msgRef - index 6
                result[6] = row[6] != null ? row[6].toString() : "";

                // relatedRef - index 7
                result[7] = row[7] != null ? row[7].toString() : "";

                // emptyField - index 8
                result[8] = "";

            } catch (Exception e) {
                log.error("Error mapping row: {}", e.getMessage());
                Arrays.fill(result, "");
            }

            return result;
        };
    }

    private static String formatDateTime(Object obj) {
        if (obj == null) return "";

        try {
            if (obj instanceof Timestamp) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return sdf.format((Timestamp) obj);
            } else if (obj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return sdf.format((Date) obj);
            } else if (obj instanceof LocalDateTime) {
                return ((LocalDateTime) obj).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String toStr(Object o) {
        // Tối ưu: tránh gọi toString() không cần thiết
        if (o == null) return "";

        if (o instanceof LocalDateTime) {
            return ((LocalDateTime) o).toString();
        } else if (o instanceof LocalDate) {
            return ((LocalDate) o).toString();
        } else if (o instanceof Date) {
            return ((Date) o).toString();
        } else if (o instanceof Timestamp) {
            return ((Timestamp) o).toLocalDateTime().toString();
        }

        return o.toString();
    }
}
