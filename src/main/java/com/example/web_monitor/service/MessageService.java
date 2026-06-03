package com.example.web_monitor.service;

import com.example.web_monitor.dto.*;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.filter.FilterDefinition;
import com.example.web_monitor.filter.specification.GenericSpecification;
import com.example.web_monitor.mapper.MessageMapper;
import com.example.web_monitor.mapper.TxMsgLogDetailMapper;
import com.example.web_monitor.model.entities.*;
import com.example.web_monitor.repository.*;
import com.example.web_monitor.utils.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class MessageService {
    private static final Logger log = getLogger(lookup().lookupClass());
    private final MessageRepository messageRepository;
    private final MessageHistRepository messageHistRepository;
    private final RptLogService rptLogService;
    private final SentMsgRepository sentMsgRepository;
    private final AckNakRepository ackNakRepository;
    private final MessageMapper messageMapper;
    private final TxMsgLogDetailMapper txMsgLogDetailMapper;
    private final TxMsgLogDetailRepository txMsgLogDetailRepository;
    private final TxMsgLogDetailHistRepository txMsgLogDetailHistRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String bootstrapServers;
    private final ParticipantService participantService;
    private final DefschemaService defschemaService;
    private final RegUsersService regUsersService;

    // Cache để tránh kiểm tra lại nhiều lần cho cùng một topic (thread-safe)
    private final Set<String> createdTopicsCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Timeout mặc định cho các thao tác AdminClient (10 giây)
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    // Số partition và replication factor mặc định khi tạo topic mới
    private static final int DEFAULT_PARTITIONS = 1;
    private static final short DEFAULT_REPLICATION_FACTOR = 1;

    public MessageService(MessageRepository messageRepository, MessageHistRepository messageHistRepository, SentMsgRepository sentMsgRepository, AckNakRepository ackNakRepository, MessageMapper messageMapper,
                          TxMsgLogDetailRepository txMsgLogDetailRepository, DefschemaService defschemaService, TxMsgLogDetailHistRepository txMsgLogDetailHistRepository, TxMsgLogDetailMapper txMsgLogDetailMapper,
                          KafkaTemplate<String, String> kafkaTemplate, ParticipantService participantService,RptLogService rptLogService,RegUsersService regUsersService,
                          @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.messageRepository = messageRepository;
        this.messageHistRepository = messageHistRepository;
        this.sentMsgRepository = sentMsgRepository;
        this.ackNakRepository = ackNakRepository;
        this.messageMapper = messageMapper;
        this.txMsgLogDetailRepository = txMsgLogDetailRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.bootstrapServers = bootstrapServers;
        this.txMsgLogDetailHistRepository = txMsgLogDetailHistRepository;
        this.txMsgLogDetailMapper = txMsgLogDetailMapper;
        this.participantService = participantService;
        this.defschemaService = defschemaService;
        this.rptLogService=rptLogService;
        this.regUsersService = regUsersService;
    }

    public List<FilterDefinition> getHistoryFilterConfig() {
        // 1. Tĩnh: Trạng thái (Giữ nguyên như bạn làm)
        List<FilterDefinition.Option> statusOptions = List.of(
                FilterDefinition.Option.builder().value("E").label("filter.option.status.E").build(),
                FilterDefinition.Option.builder().value("N").label("filter.option.status.N").build(),
                FilterDefinition.Option.builder().value("P.I").label("filter.option.status.P").build(),
                FilterDefinition.Option.builder().value("P.E.N").label("filter.option.status.P.E.N").build(),
                FilterDefinition.Option.builder().value("P.E.S").label("filter.option.status.P.E.S").build(),
                FilterDefinition.Option.builder().value("P.E.unknown").label("filter.option.status.unknown").build()
        );

        // 2. Động: Lấy từ DB (Vì BIC Code có rất nhiều và có thể thay đổi)
        List<FilterDefinition.Option> bicOptions = participantService.getAllBiccode().stream()
                .map(bic -> FilterDefinition.Option.builder()
                        .value(bic)
                        .label(bic)
                        .build())
                .toList();
        List<FilterDefinition.Option> usernameOptions = regUsersService.findUsrname().stream()
                .map(vsd -> FilterDefinition.Option.builder()
                        .value(vsd)
                        .label(vsd)
                        .build())
                .toList();
        List<FilterDefinition.Option> msgTypeOptions = defschemaService.getAllSchName().stream()
                .map(type -> FilterDefinition.Option.builder()
                        .value(type)
                        .label(type)
                        .build())
                .toList();

        return List.of(
                FilterDefinition.builder().field("refSeqId").label("refSeqId").type(FilterDefinition.Type.TEXT).build(),
                FilterDefinition.builder().field("stpRefId").label("stpRefId ").type(FilterDefinition.Type.TEXT).build(),
                // 2. Loại điện (TRFCODE)
                FilterDefinition.builder().field("trfCode").label("trfCode").type(FilterDefinition.Type.SELECT).options(msgTypeOptions).build(),

                //   usrName và Receiver
                FilterDefinition.builder()
                        .field("usrName")
                        .label("usrName")
                        .type(FilterDefinition.Type.SELECT)
                        .options(usernameOptions)
                        .build(),
                FilterDefinition.builder()
                        .field("refReceiver")
                        .label("refReceiver")
                        .type(FilterDefinition.Type.SELECT)
                        .options(bicOptions)
                        .build(),

                //  Status
                FilterDefinition.builder()
                        .field("status")
                        .label("status")
                        .type(FilterDefinition.Type.SELECT)
                        .options(statusOptions)
                        .build(),
                // 7. Ngày giao dịch (TXDATE)
                FilterDefinition.builder().field("txDate").label("txDate").type(FilterDefinition.Type.DATE_RANGE).build()
        );
    }

    private void normalizeStatusFilter(Map<String, Object> filters) {
        Object statusObj = filters.get("status");

        if (!ObjectUtils.isValid(statusObj)) {
            filters.remove("status");
            filters.remove("inOutFlag");
            filters.remove("sentStatus");
            return;
        }

        String[] parts = statusObj.toString().trim().split("\\.");

        // P.I
        if (parts.length == 2) {
            filters.put("status", "P");          // txmsglog.status
            filters.put("inOutFlag", parts[1]);  // I
            filters.remove("sentStatus");
        }

        // P.E.N / P.E.S
        else if (parts.length == 3) {
            filters.put("status", "P");          // txmsglog.status
            filters.put("inOutFlag", "E");        // txmsglog.inOutFlag
            filters.put("sentStatus", parts[2]); // N / S (logic-only)
        }
    }


    // 2. XỬ LÝ SEARCH: Nhận Map params -> Trả về Page
    public Page<MessageDto> searchMessages(Map<String, String> params, int page, int size) {
        // 1. Tạo bản sao của params để có thể chỉnh sửa (params gốc thường là immutable)
        Map<String, Object> filters = new HashMap<>(params);

        // 2. LOGIC MỚI: Tự động điền ngày hiện tại nếu thiếu 1 trong 2 đầu
        String today = LocalDate.now().toString(); // Lấy ngày hôm nay yyyy-MM-dd

        // Tạo list key để duyệt (tránh lỗi ConcurrentModification khi sửa map trong vòng lặp)
        List<String> keys = new ArrayList<>(filters.keySet());

        for (String key : keys) {
            // Trường hợp 1: Có "From" nhưng thiếu "To"
            if (key.endsWith("From")) {
                String baseName = key.substring(0, key.length() - 4);
                String toKey = baseName + "To";

                // Nếu không có To hoặc To rỗng -> Gán To = Today
                Object fromVal = filters.get(key);
                Object toVal = filters.get(toKey);

                if (ObjectUtils.isValid(fromVal) && !ObjectUtils.isValid(toVal)) {
                    filters.put(toKey, today);
                }
            }
            // Trường hợp 2: Có "To" nhưng thiếu "From"
            else if (key.endsWith("To")) {
                String baseName = key.substring(0, key.length() - 2);
                String fromKey = baseName + "From";

                // Nếu không có From hoặc From rỗng -> Gán From = Today
                Object toVal = filters.get(key);
                Object fromVal = filters.get(fromKey);

                if (ObjectUtils.isValid(toVal) && !ObjectUtils.isValid(fromVal)) {
                    filters.put(fromKey, today);
                }
            }
        }
        // Lọc bỏ các param không phải filter (như page, size)
        filters.remove("page");
        filters.remove("size");
        normalizeStatusFilter(filters);
        String sentStatusTemp = (String) filters.remove("sentStatus");

        Page<MessageEntity> pageEntity;

        if (sentStatusTemp != null && !sentStatusTemp.isEmpty()) {
            log.info(
                    "[SEARCH][CASE-1] P.E.* (has sentmsg) | sentStatus={}",
                    sentStatusTemp
            );
            pageEntity = messageRepository.searchWithSentStatus(
                    sentStatusTemp,
                    (String) filters.get("refSeqId"),
                    (String) filters.get("stpRefId"),
                    (String) filters.get("trfCode"),
                    (String) filters.get("usrName"),
                    (String) filters.get("refReceiver"),
                    parseFromDate(filters.get("txDateFrom")),
                    parseToDate(filters.get("txDateTo")),
                    PageRequest.of(page, size, Sort.by("txDate").descending())
            );
        } else if ("P".equals(filters.get("status"))
                && "I".equals(filters.get("inOutFlag"))) {
            log.info(
                    "[SEARCH][CASE-2] P.I (pending inbound, no sentmsg)"
            );

            pageEntity = messageRepository.findPendingInboundWithoutSent(
                    (String) filters.get("refSeqId"),
                    (String) filters.get("stpRefId"),
                    (String) filters.get("trfCode"),
                    (String) filters.get("usrName"),
                    (String) filters.get("refReceiver"),
                    parseFromDate(filters.get("txDateFrom")),
                    parseToDate(filters.get("txDateTo")),
                    PageRequest.of(page, size, Sort.by("txDate").descending())
            );
        } else {
            log.info(
                    "[SEARCH][CASE-3] Default findAll | filters={}",
                    filters
            );
            Specification<MessageEntity> spec = GenericSpecification.create(filters);
            pageEntity = messageRepository.findAll(
                    spec,
                    PageRequest.of(page, size, Sort.by("txDate").descending())
            );
        }

// Lấy danh sách composite key (orgSeqId|refSender)
        List<String> compositeKeys = pageEntity.getContent().stream()
                .filter(e -> "E".equals(e.getInOutFlag()) && "P".equals(e.getStatus()))
                .map(e -> {
                    if (e.getOrgSeqId() != null && e.getRefSender() != null) {
                        return e.getOrgSeqId() + "|" + e.getRefSender();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

// Lấy danh sách orgseqid riêng để query
        List<String> orgseqids = compositeKeys.stream()
                .map(key -> key.split("\\|")[0])
                .distinct()
                .toList();

        Map<String, String> sentMsgStatusMap = new HashMap<>();

        if (sentStatusTemp != null && !sentStatusTemp.isEmpty()) {
            log.info("sentStatusTemp = '{}'", sentStatusTemp);
            log.info("orgseqids = {}", orgseqids);

            // Có filter status
            sentMsgStatusMap = sentMsgRepository
                    .findByCoreRefIdInAndStatus(orgseqids, sentStatusTemp)
                    .stream()
                    .peek(sent -> {
                        if (!"N".equals(sent.getStatus())) {
                            log.error(">>>>>> FOUND NON-N STATUS: {}|{} = {}",
                                    sent.getCoreRefId(), sent.getRefSender(), sent.getStatus());
                        }
                    })
                    .filter(sent -> sent.getCoreRefId() != null && sent.getRefSender() != null)
                    .collect(Collectors.toMap(
                            sent -> sent.getCoreRefId() + "|" + sent.getRefSender(),
                            SentMsgEntity::getStatus,
                            (a, b) -> {

                                return a;
                            }
                    ));
            log.info("=============Filtered by status {}: {}", sentStatusTemp, sentMsgStatusMap);
        } else {
            // Không filter - lấy tất cả sentMsg
            sentMsgStatusMap = sentMsgRepository
                    .findByCoreRefIdIn(orgseqids)
                    .stream()
                    .filter(sent -> sent.getCoreRefId() != null && sent.getRefSender() != null)
                    .collect(Collectors.toMap(
                            sent -> sent.getCoreRefId() + "|" + sent.getRefSender(),
                            SentMsgEntity::getStatus,
                            (a, b) -> {

                                return a;
                            }
                    ));
            log.info("=============All sentMsg: {}", sentMsgStatusMap);
        }
        // Map DTO
        Map<String, String> finalSentMsgStatusMap = sentMsgStatusMap;
        return pageEntity.map(entity -> {
            MessageDto dto = messageMapper.toDto(entity);
            if ("UNDEFINED".equals(dto.getRefCode())) {
                String refSender = entity.getRefSender();
                if (refSender != null && !refSender.isBlank()) {
                    // case bắt đầu bằng VSDC
                    if (refSender.startsWith("VSDC")) {
                        String temp = refSender.substring(4);
                        dto.setRefCode(
                                temp.endsWith("X")
                                        ? temp.substring(0, temp.length() - 1)
                                        : temp
                        );
                    } else {

                        dto.setRefCode(refSender);
                    }
                }
            }
                // ======================
            // Chỉ xử lý message E đã processed
            if ("E".equals(entity.getInOutFlag()) && "P".equals(entity.getStatus())) {
                String compositeKey = entity.getOrgSeqId() + "|" + entity.getRefSender();
                String sentStatus = finalSentMsgStatusMap.get(compositeKey);

                if (sentStatus != null) {
                    dto.setStatus("P.E." + sentStatus);
                    log.debug("Mapped {} -> P.E.{}", compositeKey, sentStatus);
                } else {
                    // Không có sentStatus -> UNKNOWN
                    dto.setStatus("P.E.UNKNOWN");
                    log.debug("Mapped {} -> P.E.UNKNOWN (no sentmsg found)", compositeKey);
                }
            }

            return dto;
        });


    }

    private LocalDateTime parseFromDate(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }

        LocalDate d = LocalDate.parse(value.toString());
        return d.atStartOfDay();   // 00:00:00
    }

    private LocalDateTime parseToDate(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }

        LocalDate d = LocalDate.parse(value.toString());
        return d.atTime(23, 59, 59); // 23:59:59
    }


    public MessageDto findMsgDetailBySeqId(String seqId) {
        // 1. Lấy message trước
        MessageEntity message = messageRepository
                .findBySeqId(seqId)
                .orElse(null);
        if (message == null) {
            return null;
        }
        MessageDto dto = messageMapper.toDto(message);
        if ("E".equals(message.getInOutFlag())) {

            sentMsgRepository.findByCoreRefIdAndRefSender(dto.getOrgSeqId(),dto.getRefSender())
                    .ifPresent(sentMsg -> {
                        dto.setStatus("P.E." + sentMsg.getStatus());
                    });
        }
        return dto;
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public MessageDto resendMessage(Long autoId) {
        MessageEntity entity = messageRepository.findByAutoId(autoId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi với id=" + autoId));
        if (entity.getInOutFlag().equals("E")) {
            throw new BusinessException(ErrorCode.RESEND_OUT_MESSAGE_FORBIDDEN);
        }
        cleanEntityForResend(entity.getOrgSeqId());

        String key = UUID.randomUUID().toString(); // key
        String payload = KafkaUtils.updateStatusKeepFormat(entity.getMsgBody(), "N");

        if (payload == null || payload.isBlank()) {
            throw new IllegalStateException("MSG body trống, không thể gửi lại Kafka");
        }

        String usrName = entity.getUsrName();
        if (usrName == null || usrName.isBlank()) {
            throw new IllegalStateException("USRNAME trống, không thể xác định topic để gửi lại Kafka");
        }

        // build header JSON kiểu Spring
        Map<String, String> headers = Map.of(
                "spring_json_header_types", "{\"QUEUE_DESTINATION\":\"java.lang.String\"}",
                "QUEUE_DESTINATION", "disruptor-vm:tcpserver.request"
        );

        // topic dynamic
        String dynamicTopic = KafkaUtils.buildResendTopic(usrName);
        ensureTopicExists(dynamicTopic);

        // tạo ProducerRecord với key, payload, headers
        ProducerRecord<String, String> record = new ProducerRecord<>(dynamicTopic, key, payload);
        headers.forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        kafkaTemplate.send(record);
                        log.info("Resent message {} to topic {}", autoId, dynamicTopic);
                    }
                }
        );
        log.info("Resent message {} to topic {} with key {} and usrName {}", autoId, dynamicTopic, key, usrName);

        return messageMapper.toDto(entity);
    }

    @Transactional
    public void cleanEntityForResend(String orgSeqId) {

        // 1. Lấy message
        List<MessageEntity> messages =
                messageRepository.findLiteByOrgSeqId(orgSeqId);
        rptLogService.deleteByCoreRefId(orgSeqId);

        if (messages.isEmpty()) {
            return;
        }

        // 2. Check pending
        boolean hasPending = messages.stream()
                .anyMatch(m -> "P".equals(m.getStatus()));

        if (hasPending) {
            log.info("OrgSeqId {} has PENDING message, notified FE", orgSeqId);
            // TODO: push event
        }

        // 3. Build history list (IN MEMORY)
//        List<MessageHistEntity> histList = messages.stream()
//                .map(m -> {
//                    MessageHistEntity h = messageMapper.toHistEntity(m);
//                    h.setStatus("R");
//                    return h;
//                })
//                .toList();

        // 4. Save history BATCH
//        messageHistRepository.saveAll(histList);

        // 5. Delete message BATCH
        messageRepository.deleteByOrgSeqId(orgSeqId);

        // 6. TxMsgLogDetail → hist
        List<TxMsgLogDetail> details =
                txMsgLogDetailRepository.findAllByOrgSeqId(orgSeqId);

        if (!details.isEmpty()) {
//            txMsgLogDetailHistRepository.saveAll(
//                    details.stream()
//                            .map(txMsgLogDetailMapper::toHistEntity)
//                            .toList()
//            );
            txMsgLogDetailRepository.deleteByOrgSeqId(orgSeqId);
        }

        // 7. Delete related tables (1 lần)
        sentMsgRepository.deleteByCoreRefId(orgSeqId);
        ackNakRepository.deleteAllBySeqid(orgSeqId);
    }


    /**
     * Đảm bảo topic tồn tại trong Kafka. Nếu chưa có thì tự động tạo.
     * Sử dụng cache để tránh kiểm tra lại nhiều lần cho cùng một topic.
     *
     * @param topicName Tên topic cần kiểm tra/tạo
     * @throws IllegalStateException nếu không thể kiểm tra hoặc tạo topic
     */
    public void ensureTopicExists(String topicName) {
        // Kiểm tra cache trước để tránh gọi AdminClient nhiều lần
        if (createdTopicsCache.contains(topicName)) {
            log.debug("Topic {} đã được cache, bỏ qua kiểm tra", topicName);
            return;
        }

        try (AdminClient adminClient = createAdminClient()) {
            // Kiểm tra topic đã tồn tại chưa
            Set<String> existingTopics = adminClient.listTopics().names().get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (existingTopics.contains(topicName)) {
                // Topic đã tồn tại, thêm vào cache
                createdTopicsCache.add(topicName);
                log.debug("Topic {} đã tồn tại trong Kafka", topicName);
                return;
            }

            // Topic chưa tồn tại, tạo mới
            log.info("Topic {} chưa tồn tại, đang tạo topic mới với {} partitions và replication factor {}...",
                    topicName, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR);

            NewTopic newTopic = new NewTopic(topicName, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR);

            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));

            // Đợi kết quả tạo topic
            KafkaFuture<Void> future = result.values().get(topicName);
            future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Thêm vào cache sau khi tạo thành công
            createdTopicsCache.add(topicName);
            log.info("Đã tạo thành công topic: {} với {} partitions và replication factor {}",
                    topicName, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR);

        } catch (ExecutionException e) {
            // Nếu topic đã được tạo bởi thread khác trong lúc đang tạo
            if (e.getCause() instanceof TopicExistsException) {
                log.info("Topic {} đã được tạo bởi process khác", topicName);
                createdTopicsCache.add(topicName);
            } else {
                log.error("Lỗi khi tạo topic {}: {}", topicName, e.getMessage(), e);
                throw new IllegalStateException("Không thể tạo topic: " + topicName, e);
            }
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra/tạo topic {}: {}", topicName, e.getMessage(), e);
            throw new IllegalStateException("Không thể kiểm tra/tạo topic: " + topicName, e);
        }
    }

    /**
     * Tạo AdminClient để quản lý topics trong Kafka.
     *
     * @return AdminClient instance
     */
    private AdminClient createAdminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, DEFAULT_TIMEOUT_SECONDS * 1000);
        return AdminClient.create(configs);
    }

    /**
     * Lấy tiến trình xử lý theo orgSeqId (không cần nhập thêm filter khác).
     */
    public List<TraceMsgLogDto> getTraceByOrgSeqId(String orgSeqId) {
        if (orgSeqId == null || orgSeqId.isBlank()) {
            return List.of();
        }
        List<TxMsgLogDetail> entities = txMsgLogDetailRepository.findByOrgSeqIdOrderByTxDateAsc(orgSeqId);
        List<TraceMsgLogDto> result = new ArrayList<>();
        for (TxMsgLogDetail e : entities) {
            String status = e.getStatus();
            result.add(TraceMsgLogDto.builder()
                    .autoId(e.getAutoId() != null ? e.getAutoId().toString() : null)
                    .orgSeqId(e.getOrgSeqId())
                    .actionTime(DateUtils.formatDate(e.getTxDate()))
                    .statusCode(TraceStatusMapper.mapDetailStatusCode(status))
                    .statusText(TraceStatusMapper.mapDetailStatusText(status))
                    .msgType(TraceStatusMapper.mapMsgTypeByStatus(status))
                    .build());
        }
        return result;
    }

    public List<MessageDetailDto> getMessageDetailByOrgSeqId(String orgSeqId, long autoId) {

        List<TxMsgLogDetail> details =
                txMsgLogDetailRepository.findAllByOrgSeqIdAndAutoId(orgSeqId, autoId);

        if (details.isEmpty()) {
            throw new RuntimeException(
                    "Không tìm thấy TXMSGLOG_DETAIL với orgSeqId = "
                            + orgSeqId + " và autoId = " + autoId
            );
        }

        return details.stream()
                .map(d -> MessageDetailDto.builder()
                        .orgSeqId(d.getOrgSeqId())
                        .status(d.getStatus())
                        .txDate(d.getTxDate())
                        .build()
                )
                .collect(Collectors.toList());
    }


    public Page<StatisticMessageDto> getStatisticForDashboard(Pageable pageable, String keyword,
                                                              Integer minutes) {

        int m = (minutes == null || minutes <= 0) ? 30 : minutes;
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minus(Duration.ofMinutes(m));

        return messageRepository.statisticForDashboard(
                start,
                end,
                keyword,
                pageable
        );
    }


    public StatisticMessageSummaryDto getTodayTotalStatistic() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Long totalIn = messageRepository.countTotalInBetween(start, end);
        Long totalOut = messageRepository.countTotalOutBetween(start, end);

        return new StatisticMessageSummaryDto(
                totalIn != null ? totalIn : 0,
                totalOut != null ? totalOut : 0
        );
    }
    @Transactional
    public List<Object[]> findByRefSenderAndTxDateBetweenWithPagination(
            String refSender,
            String refSenderBIC,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {
// Thêm log để xem dữ liệu thực tế
        List<Object[]> results = messageRepository.findByRefSenderAndTxDateBetweenWithPagination( refSender,refSenderBIC, start, end, pageable);

        return results;
    }
    public int getError(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return messageRepository.countErrorsBetween(startOfDay, endOfDay);
    }

    public Map<String, Object> realtimeByMinute(LocalDateTime startOfDay, LocalDateTime endOfDay, int interval) {
        List<TxDateDto> rows = messageRepository
                .findTxDateOnly(startOfDay, endOfDay);

        if (rows.isEmpty()) {
            return Map.of("labels", List.of(), "data", List.of());
        }

        LocalDateTime startTime = rows.get(0).getTxDate();

        Map<LocalDateTime, Long> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        m -> {
                            long minutesSinceStart = Duration.between(startTime, m.getTxDate()).toMinutes();
                            long slotIndex = minutesSinceStart / interval;
                            return startTime.plusMinutes(slotIndex * interval);
                        },
                        Collectors.counting()
                ));

        List<LocalDateTime> sortedTimes = new ArrayList<>(grouped.keySet());
        sortedTimes.sort(Comparator.naturalOrder());

        List<String> labels = sortedTimes.stream()
                .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();

        List<Long> data = sortedTimes.stream()
                .map(grouped::get)
                .toList();

        return Map.of("labels", labels, "data", data);
    }


    public List<DailyElectricDto> getElectricByDay(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            LocalDate today = LocalDate.now();
            start = today.minusDays(9).atStartOfDay();
            end = today.plusDays(1).atStartOfDay();
        }
        return messageRepository.countElectricByDay(start, end).stream().collect(Collectors.groupingBy(r -> {
            Object obj = r[0];
            if (obj instanceof Date d) {
                return d.toLocalDate();
            } else if (obj instanceof Timestamp ts) {
                return ts.toLocalDateTime().toLocalDate();
            } else if (obj instanceof java.util.Date d) {
                return new Date(d.getTime()).toLocalDate();
            }
            throw new IllegalArgumentException("Unsupported date type");
        }, Collectors.summingLong(r -> ((Number) r[1]).longValue()))).entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> new DailyElectricDto(e.getKey(), e.getValue())).toList();


    }


    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public void resendPending(List<String> orgSeqIds) {
        long okCount =
                sentMsgRepository
                        .countDistinctCoreRefIdStatusN(orgSeqIds);

        if (okCount != orgSeqIds.size()) {
            throw new BusinessException(
                    ErrorCode.SOME_MESSAGE_NOT_ELIGIBLE_FOR_RESEND
            );
        }
        List<MessageEntity> list =
                messageRepository.findByOrgSeqIdInAndInOutFlag(
                        orgSeqIds, "I"
                );
        if (list.isEmpty()) {
            log.info("No pending message to resend");
            return;
        }
        // Clean DB
        cleanEntitiesForResendBatch(orgSeqIds);
        // Đăng ký gửi Kafka SAU COMMIT
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        list.forEach(entity -> {
                            try {
                                sendKafkaOnly(entity);
                            } catch (Exception ex) {
                                log.error(
                                        "Resend failed autoId={}",
                                        entity.getAutoId(),
                                        ex
                                );
                            }
                        });

                        log.info("Resend completed, total={}", list.size());
                    }
                }
        );
    }

    @Transactional
    public void cleanEntitiesForResendBatch(List<String> orgSeqIds) {
        List<MessageEntity> messages =
                messageRepository.findLiteByOrgSeqIdIn(orgSeqIds);
//        List<MessageHistEntity> histList =
//                messages.stream()
//                        .map(m -> {
//                            MessageHistEntity h =
//                                    messageMapper.toHistEntity(m);
//                            h.setStatus("R");
//                            return h;
//                        }).toList();
//
//        messageHistRepository.saveAll(histList);
        messageRepository.deleteByOrgSeqIdIn(orgSeqIds);

        List<TxMsgLogDetail> details =
                txMsgLogDetailRepository
                        .findAllByOrgSeqIdIn(orgSeqIds);

//        txMsgLogDetailHistRepository.saveAll(
//                details.stream()
//                        .map(txMsgLogDetailMapper::toHistEntity)
//                        .toList()
//        );

        txMsgLogDetailRepository.deleteByOrgSeqIdIn(orgSeqIds);
        sentMsgRepository.deleteByCoreRefIdIn(orgSeqIds);
        ackNakRepository.deleteAllBySeqidIn(orgSeqIds);
        rptLogService.deleteByCoreRefIds(orgSeqIds);
    }

    private void sendKafkaOnly(MessageEntity entity) {
        String key = UUID.randomUUID().toString();
        String payload = KafkaUtils.updateStatusKeepFormat(
                entity.getMsgBody(), "N"
        );
        if (payload == null || payload.isBlank()) {
            return;
        }

        String usrName = entity.getUsrName();
        if (usrName == null || usrName.isBlank()) {
            return;
        }

        Map<String, String> headers = Map.of(
                "spring_json_header_types",
                "{\"QUEUE_DESTINATION\":\"java.lang.String\"}",
                "QUEUE_DESTINATION",
                "disruptor-vm:tcpserver.request"
        );

        String dynamicTopic =
                KafkaUtils.buildResendTopic(usrName);
        ensureTopicExists(dynamicTopic);
        ProducerRecord<String, String> record =
                new ProducerRecord<>(
                        dynamicTopic,
                        key,
                        payload
                );

        headers.forEach((k, v) ->
                record.headers().add(
                        new RecordHeader(
                                k,
                                v.getBytes(StandardCharsets.UTF_8)
                        )
                )
        );

        kafkaTemplate.send(record);
    }
    public List<StatusCardDTO> getStatusCards(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Object[]> rows = messageRepository.countByBusinessStatus(fromDate,toDate);

        if (rows.isEmpty()) {
            return List.of(
                    new StatusCardDTO("P.I",   "filter.option.status.P",      "blue",   0L),
                    new StatusCardDTO("N",     "filter.option.status.N",      "yellow", 0L),
                    new StatusCardDTO("P.E.N", "filter.option.status.P.E.N",  "orange", 0L),
                    new StatusCardDTO("P.E.S", "filter.option.status.P.E.S",  "green",  0L),
                    new StatusCardDTO("E",     "filter.option.status.E",      "red",    0L),
                    new StatusCardDTO("unknown", "filter.option.status.unknown", "purple", 0L)
            );
        }

        Object[] r = rows.get(0);

        Long nCount = toLong(r[0]);           // N
        Long eCount = toLong(r[1]);           // E
        Long pICount = toLong(r[2]);          // P.I
        Long pENCount = toLong(r[3]);         // P.E.N
        Long pESCount = toLong(r[4]);         // P.E.S
        Long pENoSentCount = toLong(r[5]);    // P.E (không sentmsg)
        Long totalPE = pENCount + pESCount + pENoSentCount;
        Long totalTxMsgLog = nCount + eCount + pICount + totalPE;

        log.info("N={}, E={}, P.I={}, P.E.N={}, P.E.S={}, P.E.NO_SENT={}, Total={}",
                nCount, eCount, pICount, pENCount, pESCount, pENoSentCount, totalTxMsgLog);

        return List.of(
                new StatusCardDTO("P.I",   "filter.option.status.P",      "blue",   pICount),
                new StatusCardDTO("N",     "filter.option.status.N",      "yellow", nCount),
                new StatusCardDTO("P.E.N", "filter.option.status.P.E.N",  "orange", pENCount),
                new StatusCardDTO("P.E.S", "filter.option.status.P.E.S",  "green",  pESCount),
                new StatusCardDTO("E",     "filter.option.status.E",      "red",    eCount),
                new StatusCardDTO("P.E.UNKNOWN", "filter.option.status.unknown", "purple", pENoSentCount)
        );
    }

    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public List<StatusCardDTO> getStatusCardsAckNak() {

        List<Object[]> rows = ackNakRepository.countByStatus();

        long countS = 0;
        long countN = 0;

        for (Object[] r : rows) {
            String status = (String) r[0];
            Long total = ((Number) r[1]).longValue();
            if ("S".equals(status)) {
                countS = total;
            } else if ("N".equals(status)) {
                countN = total;
            }
        }
        return List.of(
                new StatusCardDTO("S", "acknak.status.S", "blue", countS),
                new StatusCardDTO("N", "acknak.status.N", "yellow", countN)
        );
    }
    public Optional<MessageEntity> findResponseMessage(
            String orgSeqId) {

        if (orgSeqId == null || orgSeqId.isBlank()) {
            return Optional.empty();
        }

        return messageRepository
                .findFirstResponseMessage(orgSeqId);
    }
}