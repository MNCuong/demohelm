package com.example.web_monitor.kafka;


import com.example.web_monitor.dto.KafkaMessageDto;
import com.example.web_monitor.service.*;
import com.example.web_monitor.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class MessageConsumer {

    private final KafkaConsumer<String, String> consumer;
    private final ReportProcessingService reportProcessingService;
    @Value("${kafka.topic.tcpserver.request}")
    private String tcpRequestTopic;
    @Value("${spring.jackson.time-zone}")
    private String appTimeZone;


    public MessageConsumer(KafkaConsumer<String, String> consumer, ReportProcessingService reportProcessingService, @Value("${kafka.topic.tcpserver.request}") String tcpRequestTopic) {
        this.consumer = consumer;
        this.reportProcessingService = reportProcessingService;
        this.tcpRequestTopic = tcpRequestTopic;
    }

    /**
     * Lấy n bản ghi từ partition + offset
     */
    public List<KafkaMessageDto> getMessagesFromOffset(int partition, long offset, int limit) {
        org.apache.kafka.common.TopicPartition tp = new org.apache.kafka.common.TopicPartition(tcpRequestTopic, partition);

        consumer.assign(Collections.singletonList(tp));
        consumer.seek(tp, offset);

        List<KafkaMessageDto> page = new ArrayList<>();
        int polled = 0;

        while (polled < limit) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            if (records.isEmpty()) break;

            for (ConsumerRecord<String, String> record : records) {
                KafkaMessageDto dto = new KafkaMessageDto(
                        record.key(),
                        record.value(),
                        record.headers().toString(),
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(record.timestamp()),
                                ZoneId.of(appTimeZone)),
                        record.offset()
                );
                page.add(dto);
                polled++;
                if (polled >= limit) break;
            }
        }

        page.sort(Comparator.comparingLong(KafkaMessageDto::getOffset).reversed());
        return page;
    }

    /**
     * Offset mới nhất
     */
    public long getLatestOffset(int partition) {
        org.apache.kafka.common.TopicPartition tp = new org.apache.kafka.common.TopicPartition(tcpRequestTopic, partition);

        consumer.assign(Collections.singletonList(tp));
        consumer.seekToEnd(Collections.singletonList(tp));
        long latest = consumer.position(tp) - 1;
        return latest;
    }

    /**
     * Offset sớm nhất
     */
    public long getEarliestOffset(int partition) {
        org.apache.kafka.common.TopicPartition tp = new org.apache.kafka.common.TopicPartition(tcpRequestTopic, partition);

        consumer.assign(Collections.singletonList(tp));
        consumer.seekToBeginning(Collections.singletonList(tp));
        long earliest = consumer.position(tp);
        return earliest;
    }

    public KafkaMessageDto findMessageByKey(String key, int partition) {
        org.apache.kafka.common.TopicPartition tp = new org.apache.kafka.common.TopicPartition(tcpRequestTopic, partition);
        consumer.assign(Collections.singletonList(tp));

        // poll từ đầu partition
        consumer.seekToBeginning(Collections.singletonList(tp));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            if (records.isEmpty()) break;
            for (ConsumerRecord<String, String> record : records) {
                if (record.key() != null && record.key().equals(key)) {
                    KafkaMessageDto dto = new KafkaMessageDto(
                            record.key(),
                            record.value(),
                            StreamSupport.stream(record.headers().spliterator(), false)
                                    .map(h -> h.key() + "=" + new String(h.value(), StandardCharsets.UTF_8))
                                    .collect(Collectors.joining(", ")),
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(record.timestamp()),
                                    ZoneId.of(appTimeZone)),
                            record.offset()
                    );

                    return dto;
                }
            }
        }

        return null;
    }

    /**
     * Tìm range offset theo key và/or date
     * Trả về [firstOffset, lastOffset] hoặc null nếu không tìm thấy
     */
    public long[] findOffsetRange(int partition, String key, LocalDateTime dateTime) {
        String topic = "tcpserver.request";
        org.apache.kafka.common.TopicPartition tp = new org.apache.kafka.common.TopicPartition(topic, partition);
        consumer.assign(Collections.singletonList(tp));

        long rangeStart = getEarliestOffset(partition);
        long rangeEnd = getLatestOffset(partition);

        if (dateTime != null) {
            long startTs = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTs = dateTime.toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Map<org.apache.kafka.common.TopicPartition, Long> timestamps = new HashMap<>();
            timestamps.put(tp, startTs);
            var startResult = consumer.offsetsForTimes(timestamps).get(tp);

            timestamps.put(tp, endTs);
            var endResult = consumer.offsetsForTimes(timestamps).get(tp);
            if (startResult == null) {
                return null;
            }

            rangeStart = Math.max(rangeStart, startResult.offset());

            if (endResult != null) {
                rangeEnd = Math.min(rangeEnd, endResult.offset() - 1);
            }

            // Đảm bảo rangeStart không bao giờ vượt quá rangeEnd
            if (rangeStart > rangeEnd && endResult != null) {
                return null;
            }
        }

        if (key != null && !key.isEmpty()) {
            Long firstMatch = null;
            Long lastMatch = null;

            consumer.seek(tp, rangeStart);
            boolean continueScanning = true;

            while (continueScanning) {
                var records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) break;

                for (var record : records) {
                    if (record.offset() > rangeEnd) {
                        continueScanning = false;
                        break;
                    }

                    if (key.equals(record.key()) || String.valueOf(record.offset()).equals(key)) {
                        if (firstMatch == null) firstMatch = record.offset();
                        lastMatch = record.offset();
                    }
                }
            }
            return (firstMatch == null) ? null : new long[]{firstMatch, lastMatch};
        }

        return new long[]{rangeStart, rangeEnd};
    }


    @KafkaListener(
            topics = "${kafka.topic.core.report}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "1"
    )
    public void listenCoreReport(ConsumerRecord<String, String> record) {
        try {
            log.info("Start gen csv");
            reportProcessingService.processCoreReport(record);
            log.info("End gen csv");
        } catch (Throwable e) {
            log.error("Error processing report", e);
        }
    }

}
