package com.example.web_monitor.service;

import com.example.web_monitor.dto.ParsedKafkaMessageDto;
import com.example.web_monitor.kafka.MessageProducer;
import com.example.web_monitor.model.entities.RptlogEntity;
import com.example.web_monitor.utils.CsvUtils;
import com.example.web_monitor.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class ReportProcessingService {
    private final MessageService messageService;
    private final RptLogService rptLogService;
    private final ParticipantService participantService;
    private final MessageProducer messageProducer;
    @Value("${path.report}")
    private String pathReport;
    @Value("${kafka.topic.report.file}")
    private String reportFile;
    private final RegUsersService regUsersService;


    public ReportProcessingService(MessageService messageService,
                                   RptLogService rptLogService,
                                   ParticipantService participantService,
                                   MessageProducer messageProducer,  RegUsersService regUsersService) {
        this.messageService = messageService;
        this.rptLogService = rptLogService;
        this.participantService = participantService;
        this.messageProducer = messageProducer;
        this.regUsersService=regUsersService;
    }

    @Transactional
    public void processCoreReport(ConsumerRecord<String, String> record) {
        String kafkaMsg = record.value();
        long kafkaTimestamp = record.timestamp();
        String key = UUID.randomUUID().toString();
        String fileName = null;
        ParsedKafkaMessageDto parsed = parseKafkaMessage(kafkaMsg);
        RptlogEntity rptLog = rptLogService.findByCoreRefId(parsed.getTag20());
        if (!Objects.equals(rptLog.getStatus(), "0")) {
            return;
        }
        if (parsed.getTradeDateError() != null) {
            log.info("Start check null date");
            rptLog.setStatus("3");
            rptLog.setErrmsg(
                    "INVALID_TRADE_DATE: " + parsed.getTradeDateError()
            );
            rptLogService.updateRptLog(rptLog);
            log.info("RptLog after update 1- id={}, status={}, errmsg={}",
                    rptLog.getAutoid(),
                    rptLog.getStatus(),
                    rptLog.getErrmsg()
            );

            log.info("[sendMessage Kafka reject] - 1");
            messageProducer.sendMessage(
                    reportFile,
                    key,
                    "REJT." + parsed.getSenderBic() + "." + parsed.getTag20()
            );
            return;
        }
        try {
            if (!KafkaUtils.validateTradeDate(parsed.getTradeDate())) {
                log.info("Start check invalid date");
                rptLog.setStatus("3");
                rptLog.setErrmsg(
                        "INVALID_TRADE_DATE: tradeDate is null or in the future"
                );
                rptLogService.updateRptLog(rptLog);
                log.info("RptLog after update2 - id={}, status={}, errmsg={}",
                        rptLog.getAutoid(),
                        rptLog.getStatus(),
                        rptLog.getErrmsg()
                );
                log.info("[sendMessage Kafka reject] - 2");
                messageProducer.sendMessage(
                        reportFile,
                        key,
                        "REJT." + parsed.getSenderBic() + "." + parsed.getTag20()
                );
                return;
            }
            String refSenderBIC=regUsersService.findBiccode(parsed.getSenderBic());
            log.info("Start process report with batch");
            fileName = processReportWithBatch(
                    refSenderBIC,
                    parsed.getTradeDate(),
                    parsed,
                    kafkaTimestamp,
                    rptLog
            );
            log.info("End process report with batch");

        } catch (Exception ex) {
            rptLog.setStatus("E");
            rptLog.setErrmsg(ex.getMessage());
            rptLogService.updateRptLog(rptLog);
            log.error("RptLog after update3 - id={}, status={}, errmsg={}",
                    rptLog.getAutoid(),
                    rptLog.getStatus(),
                    rptLog.getErrmsg()
            );
            log.error("[sendMessage Kafka reject exception] - 3");
            messageProducer.sendMessage(reportFile, key, "REJT." + parsed.getSenderBic() + "." + parsed.getTag20());
            return;
        }
        log.info(" Start update status rptlog");
        rptLog.setStatus("1");
        rptLogService.updateRptLog(rptLog);
        log.info(" End update status rptlog");
        if (fileName.endsWith(".csv")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        String kafkaMsgTest =
                "E".equals(rptLog.getStatus())
                        ? "REJT." + parsed.getSenderBic() + "." + parsed.getTag20()
                        : fileName;

        log.info("[sendMessage Kafka reject or pack] key={}, msg={}", key, kafkaMsgTest);
        log.info("RptLog after update4 - id={}, status={}, errmsg={}",
                rptLog.getAutoid(),
                rptLog.getStatus(),
                rptLog.getErrmsg()
        );
        sendKafkaAfterCommit(key, kafkaMsgTest);
    }
    private void sendKafkaAfterCommit(String key, String msg) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Send kafka AFTER COMMIT {}", msg);
                        messageProducer.sendMessage(
                                reportFile,
                                key,
                                msg
                        );
                    }
                }
        );
    }
    @Transactional
    protected String processReportWithBatch(
            String senderBic,
            LocalDate tradeDate,
            ParsedKafkaMessageDto parsed,
            long kafkaTimestamp,
            RptlogEntity rptLog
    ) throws IOException {

        LocalDateTime start = tradeDate.atStartOfDay();
        LocalDateTime end = tradeDate.plusDays(1).atStartOfDay();

        String fileName = buildFileName(
                senderBic,
                kafkaTimestamp,
                parsed.getTag20()
        );
        rptLog.setFilename(fileName);
        Path fullPath = Paths.get(pathReport, fileName);

        // Xử lý từng batch
        int page = 0;
        int batchSize = 10000; // Batch size nhỏ để tránh OOM
        boolean hasData = false;

        Files.createDirectories(fullPath.getParent());

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fullPath.toFile()), StandardCharsets.UTF_8),
                65536)) {
            // Write headers
            writeCsvHeaders(bw);
            while (true) {
                Pageable pageable = PageRequest.of(page, batchSize, Sort.by(Sort.Direction.DESC, "txDate"));
                List<Object []> batch = messageService.findByRefSenderAndTxDateBetweenWithPagination(
                        parsed.getSenderBic(),senderBic, start, end, pageable);

                if (batch.isEmpty()) {
                    break;
                }
                hasData = true;
                for (Object[] row : batch) {
                    writeMessageToCsv(bw, row, tradeDate);
                }
                // Clear batch để giải phóng memory
                batch.clear();
                page++;
                // Log tiến độ mỗi 20 batch
                if (page % 20 == 0) {
                    log.info("Processed {} batches for {}", page, fileName);
                }
            }

            bw.flush();
        }
        if (!hasData) {
            rptLog.setStatus("3");
        }
        return fileName;
    }

    private ParsedKafkaMessageDto parseKafkaMessage(String kafkaMsg) {
        String payload = KafkaUtils.extractPayload(kafkaMsg);
        String block4 = KafkaUtils.block4(payload);
        LocalDate tradeDate = null;
        String tradeDateError = null;
        try {
            tradeDate = KafkaUtils.extractTradeDateFromBlock4(block4);
        } catch (Exception e) {
            tradeDateError = e.getMessage();
        }
        return ParsedKafkaMessageDto.builder()
                .payload(payload)
                .senderBic(KafkaUtils.refSenderBic(kafkaMsg))
                .tradeDate(tradeDate)
                .tradeDateError(tradeDateError)
                .tag20(KafkaUtils.msgRef(payload))
                .build();
    }

    private void writeCsvHeaders(BufferedWriter bw) throws IOException {
        String[] headers = CsvUtils.HEADERS;
        for (int i = 0; i < headers.length; i++) {
            writeEscaped(bw, headers[i]);
            if (i < headers.length - 1) bw.write(",");
        }
        bw.newLine();
    }

    private void writeMessageToCsv(BufferedWriter bw, Object[] row, LocalDate date) throws IOException {
        String[] values = CsvUtils.rowMapper().apply(row);
        for (int i = 0; i < values.length; i++) {
            writeEscaped(bw, values[i]);
            if (i < values.length - 1) bw.write(",");
        }
        bw.newLine();
    }

    private void writeEscaped(BufferedWriter bw, String value) throws IOException {
        if (value == null) {
            return;
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            bw.write("\"");
            bw.write(value.replace("\"", "\"\""));
            bw.write("\"");
        } else {
            bw.write(value);
        }
    }

    private String buildFileName(String senderBic, long timestamp, String refId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "PACK." + senderBic + "_" + sdf.format(new Date(timestamp)) + "." + refId + ".csv";
    }
}
