package com.example.web_monitor.service;

import com.example.web_monitor.model.entities.TxMsgLogDetail;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class KafkaMonitorService {

    private static final Logger log = getLogger(lookup().lookupClass());
    private final TxMsgLogDetailService txMsgLogDetailService;
    private final Consumer<String, String> sharedConsumer;

    private long lastAutoId = 0L;        // autoId cuối cùng đã tính trong ngày
    private long lastDelayTime = 0L;     // thời gian bản ghi cuối cùng
    private LocalDate currentDay = null; // ngày hiện tại đang tính
    private volatile double lastAvgLatency = 0.0;

    // Cache cho throughput
    private final Map<String, Long> lastCountMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastTimeMap = new ConcurrentHashMap<>();

    // Constructor Injection
    public KafkaMonitorService(@Qualifier("monitorConsumer") Consumer<String, String> sharedConsumer,
                               TxMsgLogDetailService txMsgLogDetailService) {
        this.sharedConsumer = sharedConsumer;
        this.txMsgLogDetailService = txMsgLogDetailService;
        initCache();
    }

    public Long getMessageCount(String topicName) {
        synchronized (sharedConsumer) {
            try {
                List<PartitionInfo> partitions = sharedConsumer.partitionsFor(topicName);
                if (partitions == null || partitions.isEmpty()) {
                    return 0L;
                }

                List<TopicPartition> topicPartitions = partitions.stream()
                        .map(p -> new TopicPartition(p.topic(), p.partition()))
                        .collect(Collectors.toList());

                Map<TopicPartition, Long> endOffsets = sharedConsumer.endOffsets(topicPartitions);
                Map<TopicPartition, Long> beginningOffsets = sharedConsumer.beginningOffsets(topicPartitions);

                long totalCount = 0;
                for (TopicPartition tp : topicPartitions) {
                    Long end = endOffsets.get(tp);
                    Long begin = beginningOffsets.get(tp);
                    if (end != null && begin != null) {
                        totalCount += (end - begin);
                    }
                }
                return totalCount;

            } catch (Exception e) {
                e.printStackTrace();
                return -1L;
            }
        }
    }

    public Double getThroughput(String topicName) {
        long currentCount = getMessageCount(topicName);
        long currentTime = System.currentTimeMillis();

        Long lastCount = lastCountMap.getOrDefault(topicName, 0L);
        Long lastTime = lastTimeMap.getOrDefault(topicName, 0L);

        // log.info("currentCount = {}, lastCount = {}", currentCount, lastCount);
        // log.info("currentTime = {}, lastTime = {}", currentTime, lastTime);
        lastCountMap.put(topicName, currentCount);
        lastTimeMap.put(topicName, currentTime);

        if (lastTime == 0) return 0.0;

        long deltaCount = currentCount - lastCount;
        long deltaTimeMs = currentTime - lastTime;

        if (deltaTimeMs <= 0) return 0.0;

        double throughput = (double) deltaCount / deltaTimeMs * 1000;
        return Math.round(throughput * 100.0) / 100.0;
    }

    private void initCache() {
        TxMsgLogDetail latest = txMsgLogDetailService.getMaxId();
        lastAutoId = latest != null ? latest.getAutoId() : 0L;
        lastDelayTime = 0L;
        currentDay = LocalDate.now();
        log.info("Init cache latency: lastAutoId={}, currentDay={}", lastAutoId, currentDay);
    }

    // -------------------- Latency --------------------
    public double getAvgLatency() {
        List<String> statuses = List.of("P", "N");
        LocalDate today = LocalDate.now();

        // Reset cache khi sang ngày mới
        if (currentDay == null || !currentDay.equals(today)) {
            lastAutoId = 0L;
            lastAvgLatency = 0.0;
            currentDay = today;
            log.info("Ngày mới, reset cache latency: {}", today);
        }

        List<TxMsgLogDetail> records =
                txMsgLogDetailService
                        .findInMsgDetailForLatency(
                                lastAutoId, statuses);

        if (records.isEmpty()) {
            return 0.0;
        }

        Map<Long, List<TxMsgLogDetail>> grouped =
                records.stream()
                        .collect(Collectors.groupingBy(TxMsgLogDetail::getAutoId));

        long totalDelay = 0L;
        long count = 0L;
        long maxProcessedAutoId = lastAutoId; // quan trọng

        for (Map.Entry<Long, List<TxMsgLogDetail>> entry : grouped.entrySet()) {
            Long autoId = entry.getKey();
            List<TxMsgLogDetail> list = entry.getValue();

            Optional<TxMsgLogDetail> nOpt = list.stream()
                    .filter(r -> "N".equals(r.getStatus()))
                    .min(Comparator.comparing(TxMsgLogDetail::getTxDate));

            Optional<TxMsgLogDetail> pOpt = list.stream()
                    .filter(r -> "P".equals(r.getStatus()))
                    .max(Comparator.comparing(TxMsgLogDetail::getTxDate));

            // Chỉ tính khi N và P cùng autoId
            if (nOpt.isPresent() && pOpt.isPresent()) {
                long nTime = nOpt.get().getTxDate()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                long pTime = pOpt.get().getTxDate()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                if (pTime > nTime) {
                    long latency = pTime - nTime;
                    totalDelay += latency;
                    count++;

                    maxProcessedAutoId = Math.max(maxProcessedAutoId, autoId);

                    log.info("Latency OK autoId={}, latency={} ms",
                            autoId, latency);
                }
            } else {
                log.debug("Skip autoId={} (hasN={}, hasP={})",
                        autoId, nOpt.isPresent(), pOpt.isPresent());
            }
        }
        log.info(
                "Latency summary: processedAutoIdFrom={} to={}, totalCalculated={}",
                lastAutoId + 1,
                maxProcessedAutoId,
                count
        );

        //  CHỈ update tới autoId đã xử lý xong
        lastAutoId = maxProcessedAutoId;

        if (count == 0) {
            return 0.0;
        }

        double avgDelay = totalDelay * 1.0 / count;
        lastAvgLatency = Math.round(avgDelay * 100.0) / 100.0;

        return lastAvgLatency;

    }


    public long getErrorCount(String topicName, LocalDate date) {
        synchronized (sharedConsumer) {
            try {
                List<PartitionInfo> partitions = sharedConsumer.partitionsFor(topicName);
                if (partitions == null || partitions.isEmpty()) return 0L;

                List<TopicPartition> topicPartitions = partitions.stream()
                        .map(p -> new TopicPartition(p.topic(), p.partition()))
                        .collect(Collectors.toList());

                // Gán consumer cho các partition
                sharedConsumer.assign(topicPartitions);

                Map<TopicPartition, Long> beginningOffsets = sharedConsumer.beginningOffsets(topicPartitions);
                Map<TopicPartition, Long> endOffsets = sharedConsumer.endOffsets(topicPartitions);

                long errors = 0;

                for (TopicPartition tp : topicPartitions) {
                    long begin = beginningOffsets.get(tp);
                    long end = endOffsets.get(tp);
                    sharedConsumer.seek(tp, begin);

                    while (sharedConsumer.position(tp) < end) {
                        var records = sharedConsumer.poll(java.time.Duration.ofMillis(500));
                        for (var record : records.records(tp)) {
                            // Lọc theo ngày
                            LocalDate msgDate = LocalDate.ofInstant(
                                    java.time.Instant.ofEpochMilli(record.timestamp()),
                                    java.time.ZoneId.systemDefault()
                            );
                            if (!msgDate.equals(date)) continue;

                            // Lọc theo message lỗi
                            String value = record.value();
                            if (value != null && value.toLowerCase().contains("error")) {
                                errors++;
                            }
                        }
                    }
                }

                return errors;

            } catch (Exception e) {
                e.printStackTrace();
                return 0L;
            }
        }
    }
}