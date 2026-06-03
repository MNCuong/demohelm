package com.example.web_monitor.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CsvReportUtils {

    private static final int BUFFER_SIZE = 256 * 1024;

    private CsvReportUtils() {
    }
    public static final Map<String, List<String>> MSG_TYPE_MAPPING = Map.of(
            "524.TOBA//AVAI", List.of(
                    "548.IPRC//PACK",
                    "548.IPRC//REJT"
            ),
            "524.TOBA//BLOK", List.of(
                    "548.IPRC//PACK",
                    "548.IPRC//REJT"
            ),
            "199.BLOCK", List.of(
                    "199.PACK",
                    "199.REJT"
            ),
            "199.UNBLOCK", List.of(
                    "199.PACK",
                    "199.REJT"
            )
    );

    public static void writeCsvStream(
            Object[] firstRow,
            Iterator<Object[]> restOfData,
            String[] headers,
            Path filePath,
            LocalDate date
    ) throws IOException {

        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8),
                BUFFER_SIZE)) {

            // Write headers
            for (int i = 0; i < headers.length; i++) {
                writeEscaped(bw, headers[i]);
                if (i < headers.length - 1) bw.write(",");
            }
            bw.newLine();

            if (firstRow == null) return;

            // Tạo mapper cho Object[]
            Function<Object[], String[]> mapper = CsvUtils.rowMapper();

            // Write first row
            writeRow(bw, firstRow, mapper);

            // Write remaining rows
            while (restOfData.hasNext()) {
                writeRow(bw, restOfData.next(), mapper);
            }

            bw.flush();
        }
    }

    // Sửa hàm writeRow để nhận Object[]
    private static void writeRow(BufferedWriter bw, Object[] row, Function<Object[], String[]> mapper) throws IOException {
        String[] values = mapper.apply(row);
        for (int i = 0; i < values.length; i++) {
            writeEscaped(bw, values[i]);
            if (i < values.length - 1) {
                bw.write(",");
            }
        }
        bw.newLine();
    }

    private static void writeEscaped(BufferedWriter bw, String value) throws IOException {
        if (value == null || value.isEmpty()) return;

        boolean needQuotes = false;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == ',' || c == '"' || c == '\n' || c == '\r') {
                needQuotes = true;
                break;
            }
        }

        if (!needQuotes) {
            bw.write(value);
            return;
        }

        bw.write('"');
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == '"') {
                bw.write("\"\"");
            } else if (c == '\n') {
                bw.write(' ');
            } else if (c != '\r') {
                bw.write(c);
            }
        }
        bw.write('"');
    }
}