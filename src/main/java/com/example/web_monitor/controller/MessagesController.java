package com.example.web_monitor.controller;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.dto.*;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.model.entities.AckNakEntity;
import com.example.web_monitor.model.entities.SentMsgEntity;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.service.*;

import com.example.web_monitor.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/messages")
public class MessagesController {

    private final MessageService messageService;
    private final AckNakService ackNakService;
    private final KafkaService kafkaService;
    private final SentMsgService sentMsgService;
    private final RptLogService rptLogService;

    public MessagesController(MessageService messageService, KafkaService kafkaService, AckNakService ackNakService, SentMsgService sentMsgService, RptLogService rptLogService) {
        this.messageService = messageService;
        this.ackNakService = ackNakService;
        this.kafkaService = kafkaService;
        this.sentMsgService = sentMsgService;
        this.rptLogService = rptLogService;
    }

    @GetMapping("/msg-history")
    public String viewHistory(Model model,
                              @RequestParam Map<String, String> allParams, // Hứng tất cả query param
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "50") int size) {
        // map số trang trên pagination với phân trang của Spring Data
        int pageNo = page - 1;
        // 1. Lấy param
        String fromDateStr = (String) allParams.get("txDateFrom");
        String toDateStr = (String) allParams.get("txDateTo");

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        if (ObjectUtils.isValid(fromDateStr)) {
            LocalDate fromDate = LocalDate.parse(fromDateStr);
            fromDateTime = fromDate.atStartOfDay();
        }
        if (ObjectUtils.isValid(toDateStr)) {
            LocalDate toDate = LocalDate.parse(toDateStr);
            toDateTime = toDate.plusDays(1).atStartOfDay();
        }
// filter config
        log.info("debug--------------------:{}",messageService.getHistoryFilterConfig());
        model.addAttribute(
                "filterConfigs",
                messageService.getHistoryFilterConfig()
        );

// status card
        model.addAttribute(
                "statusCounts",
                messageService.getStatusCards(fromDateTime, toDateTime)
        );
        // 2. Gửi dữ liệu đã lọc xuống View
        Page<MessageDto> pageData = messageService.searchMessages(allParams, pageNo, size);
        model.addAttribute("transactionPage", pageData);

        model.addAttribute("currentSortField", "txDate");
        model.addAttribute("currentSortDir", "desc");

        // xoa cac param khong can thiet
        allParams.remove("page");
        allParams.remove("size");

        // 3. Gửi lại các tham số user đang nhập (để giữ trạng thái form)
        model.addAttribute("currentParams", allParams);
        model.addAttribute("filters", allParams);
        model.addAttribute("currentPage", "msg-history");

        return "pages/messages/msg-history";
    }


    @GetMapping("/msg-detail")
    public String viewDetail(Model model, @RequestParam("seqId") String seqId) {

        model.addAttribute("currentPage", "msg-history");

        MessageDto msg = messageService.findMsgDetailBySeqId(seqId);
        List<MessageDetailDto> msgDetails =
                messageService.getMessageDetailByOrgSeqId(msg.getOrgSeqId(), msg.getAutoId());

        if (msgDetails.isEmpty()) {
            model.addAttribute("msg", msg);
            return "pages/messages/msg-detail";
        }

        String orgSeqId = msgDetails.get(0).getOrgSeqId();

        // 3. Xác định trạng thái theo list
        boolean hasProcessed = msgDetails.stream()
                .anyMatch(d -> "P".equals(d.getStatus()));

        boolean hasError = msgDetails.stream()
                .anyMatch(d -> "E".equals(d.getStatus()));

        // 4. Xác định trạng thái hiển thị chính
        String processStatus;
        LocalDateTime timeStampN = null;
        LocalDateTime timeStampP = null;
        LocalDateTime timeStampE = null;


        if (hasProcessed) {
            timeStampN = msgDetails.stream()
                    .filter(d -> "N".equals(d.getStatus()))
                    .map(MessageDetailDto::getTxDate)
                    .min(LocalDateTime::compareTo)
                    .orElse(msg.getTxDate());
            timeStampP = msgDetails.stream()
                    .filter(d -> "P".equals(d.getStatus()))
                    .map(MessageDetailDto::getTxDate)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }

        if (hasError) {
            timeStampN = msgDetails.stream()
                    .filter(d -> "N".equals(d.getStatus()))
                    .map(MessageDetailDto::getTxDate)
                    .min(LocalDateTime::compareTo)
                    .orElse(msg.getTxDate());
            timeStampE = msgDetails.stream()
                    .filter(d -> "E".equals(d.getStatus()))
                    .map(MessageDetailDto::getTxDate)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }
        model.addAttribute("timeStampN", timeStampN);
        model.addAttribute("timeStampP", timeStampP);
        model.addAttribute("timeStampE", timeStampE);

        if (hasError) {
            processStatus = "E";
        } else if (hasProcessed) {
            processStatus = "P";
        } else {
            processStatus = "N";
        }

        model.addAttribute("processStatus", processStatus);

        if (!"N".equals(processStatus)) {
            Optional<AckNakEntity> ackOpt = ackNakService.getAckNak(orgSeqId, msg.getRefSender());

            log.info("Total ACK/NAK records for orgSeqId {} = {}", orgSeqId,
                    ackOpt.isPresent() ? 1 : 0);

            ackOpt.ifPresent(ack -> {
                model.addAttribute("ackNakStatus", ack.getStatus());
                model.addAttribute("ackNakTime", ack.getUpdatetime());

            });
        }
        // 6. Nếu đã có P hoặc đang là E  kiểm tra trạng thái bên sent message
        Optional<SentMsgEntity> opt =
                sentMsgService.findByAutoId(msg.getAutoId().toString(), msg.getRefSender());

        opt.ifPresentOrElse(sent -> {
            log.info(
                    "[SENTMSG] FOUND | coreRefId={}, status={}, actionTime={}, updateTime={}",
                    sent.getCoreRefId(),
                    sent.getStatus(),
                    sent.getActionTime(),
                    sent.getUpdateTime()
            );

            model.addAttribute("sentMsgStatus", sent.getStatus());

            if ("S".equals(sent.getStatus())) {
                model.addAttribute("sentMsgTimeUpdate", sent.getUpdateTime());
                model.addAttribute("sentMsgTime", sent.getActionTime());
            } else if ("N".equals(sent.getStatus())) {
                model.addAttribute("sentMsgTime", sent.getActionTime());
            }

        }, () -> {
            // KHÔNG tìm thấy → set null cho FE
            log.info("[SENTMSG] NOT FOUND for autoId={}", msg.getAutoId());
            model.addAttribute("sentMsgStatus", null);
            model.addAttribute("sentMsgTime", null);
            model.addAttribute("sentMsgTimeUpdate", null);
        });


        // 7. Gán các attribute phục vụ view
        model.addAttribute("msg", msg);
        model.addAttribute("inOutFlag", msg.getInOutFlag());
        model.addAttribute("msgContent", msg.getMsgBody());
        model.addAttribute("currentPage", "msg-history");
        // timestamp của message core (dùng cho step RECEIVE)
        model.addAttribute("timeStamp", msg.getTxDate());

        // 8. Tiến trình động theo orgSeqId
        model.addAttribute("progressList",
                messageService.getTraceByOrgSeqId(msg.getOrgSeqId()));

        return "pages/messages/msg-detail";
    }


    @PostMapping("/{id}/resend")
    @TrackAction(ActionType.RESEND)
    public ResponseEntity<Map<String, Object>> resendMessage(@PathVariable("id") Long autoId) {
        MessageDto updated = messageService.resendMessage(autoId);
        return ResponseEntity.ok(Map.of(
                "message", "Resend successfully",
                "orgSeqId", updated.getOrgSeqId()
        ));
    }

    @GetMapping("/msg-acknak")
    public String msgAckNak(Model model,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "50") int size,
                            @RequestParam(defaultValue = "seqid") String sortField,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            @RequestParam Map<String, String> allParams) {
        int pageNo = Math.max(page - 1, 0);
        Page<AckNakDto> ackNakPage = ackNakService.searchMessagesAckNak(allParams, pageNo, size);

        List<String> headers = List.of(
                "page.acknak.table.header.seqId",
                "page.acknak.table.header.sendingPlace",
                "page.acknak.table.header.status",
                "page.acknak.table.header.createDate",
                "page.acknak.table.header.updateDate"
        );

        List<String> fields = List.of("seqId", "sendingPlace", "status", "createDate","updateDate");
        String idField = "seqId";
        model.addAttribute("filterConfigsAckNak", ackNakService.getHistoryFilterConfigAckNak());
        model.addAttribute("listData", ackNakPage.getContent());
        model.addAttribute("listHeaders", headers);
        model.addAttribute("listFields", fields);
        model.addAttribute("listIdField", idField);
        model.addAttribute("statusCounts", messageService.getStatusCardsAckNak());

        model.addAttribute("ackNakPage", ackNakPage);
        model.addAttribute("currentSortField", sortField);
        model.addAttribute("currentSortDir", sortDir);
        allParams.remove("page");
        allParams.remove("size");
        allParams.remove("sortField");
        allParams.remove("sortDir");
        model.addAttribute("filters", allParams);

        model.addAttribute("currentPage", "msg-acknak");
        return "pages/messages/msg-acknak";
    }

    @GetMapping("/msg-acknak-detail/{autoid}")
    public String getAckNakDetail(@PathVariable Long autoid, Model model) {
        AckNakDto.Detail ackNakDto = ackNakService.getDetail(autoid);
        model.addAttribute("ackNakDto", ackNakDto);
        model.addAttribute("currentPage", "msg-acknak");
        return "pages/messages/msg-acknak-detail";
    }

    @GetMapping("/msg-kafka")
    public String msgKafka(
            @RequestParam(value = "partition", required = false) Integer partitionParam,
            @RequestParam(value = "offset", required = false) Long offset,
            @RequestParam(value = "direction", required = false) String direction,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "dateTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            Model model) {
        int partition = (partitionParam != null) ? partitionParam : 0;

        int PAGE_SIZE = 50;
        long absoluteEarliest = kafkaService.getEarliestOffset(partition);
        long absoluteLatest = kafkaService.getLatestOffset(partition);

        // Xác định phạm vi tìm kiếm
        long rangeStart = absoluteEarliest;
        long rangeEnd = absoluteLatest;

        long[] range = kafkaService.findOffsetRange(partition, key, dateTime);
        if (range != null) {
            rangeStart = range[0];
            rangeEnd = range[1];
        } else if ((key != null && !key.isEmpty()) || dateTime != null) {
            model.addAttribute("listData", Collections.emptyList());
            return "pages/messages/msg-kafka";
        }

        long targetStartOffset;

        if (offset == null) {
            // TRANG ĐẦU lấy từ rangeEnd ngược lại PAGE_SIZE bản ghi
            targetStartOffset = Math.max(rangeStart, rangeEnd - PAGE_SIZE + 1);
        } else {
            if ("next".equals(direction)) {
                // lấy các bản ghi trước offset hiện tại
                targetStartOffset = Math.max(rangeStart, offset - PAGE_SIZE);
            } else if ("prev".equals(direction)) {
                // lấy các bản ghi sau offset hiện tại
                targetStartOffset = Math.min(rangeEnd, offset + PAGE_SIZE);
            } else {
                targetStartOffset = offset;
            }
        }

        // Tính số lượng bản ghi thực tế cần lấy để không vượt quá rangeEnd
        int actualLimit = (int) (rangeEnd - targetStartOffset + 1);
        if (actualLimit > PAGE_SIZE) actualLimit = PAGE_SIZE;
        if (actualLimit <= 0) actualLimit = 1;

        List<KafkaMessageDto> listData = kafkaService.getMessagesFromOffset(partition, targetStartOffset, actualLimit);

        // Xác định offset đầu và cuối của list vừa lấy để phân trang
        long firstOffsetInPage = listData.isEmpty() ? targetStartOffset : listData.get(listData.size() - 1).getOffset();
        long lastOffsetInPage = listData.isEmpty() ? targetStartOffset : listData.get(0).getOffset();

        model.addAttribute("listData", listData);
        model.addAttribute("key", key);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("currentOffset", firstOffsetInPage);

        model.addAttribute("hasNext", firstOffsetInPage > rangeStart);
        model.addAttribute("hasPrevious", lastOffsetInPage < rangeEnd);
        model.addAttribute("currentPage", "msg-kafka");


        return "pages/messages/msg-kafka";
    }

    @GetMapping("/msg-kafka-detail/{key}")
    public String getKafkaDetail(@PathVariable String key, Model model, @RequestParam(value = "partition", required = false) Integer partitionParam
    ) {
        int partition = (partitionParam != null) ? partitionParam : 0;

        KafkaMessageDto detail = kafkaService.getKafkaMessageDetail(key, partition);

        if (detail == null) {
            model.addAttribute("errorMessage", "No telegram bulletin with code found: " + key + " in partition " + partition);
            return "redirect:/messages/msg-kafka";
        }
        model.addAttribute("currentPage", "msg-kafka");

        model.addAttribute("detail", detail);
        return "pages/messages/msg-kafka-detail";
    }

    @PostMapping("/kafka/resend")
    @TrackAction(ActionType.RESEND)
    public ResponseEntity<?> resendKafka(@RequestBody ResendRequestDto request) {
        try {
            kafkaService.resendRawMessage(request.getMsg());
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "Resend successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @GetMapping("/follow-report")
    public String viewFollowReport(Model model,
                              @RequestParam Map<String, String> allParams,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "50") int size) {
        // map số trang trên pagination với phân trang của Spring Data
        int pageNo = page - 1;
        // 1. Lấy param
        String fromDateStr = (String) allParams.get("createdateFrom");
        String toDateStr = (String) allParams.get("createdateTo");
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        if (ObjectUtils.isValid(fromDateStr)) {
            LocalDate fromDate = LocalDate.parse(fromDateStr);
            fromDateTime = fromDate.atStartOfDay();
        }
        if (ObjectUtils.isValid(toDateStr)) {
            LocalDate toDate = LocalDate.parse(toDateStr);
            toDateTime = toDate.plusDays(1).atStartOfDay();
        }
// status card
        model.addAttribute(
                "statusCounts",
                rptLogService.getStatusCards(fromDateTime, toDateTime)
        );
        // 1. Gửi cấu hình bộ lọc xuống View
        model.addAttribute("filterConfigs", rptLogService.getHistoryFilterConfig());
        // 2. Gửi dữ liệu đã lọc xuống View
        Page<RptLogDto> pageData = rptLogService.search(allParams, pageNo, size);
        model.addAttribute("transactionPage", pageData);

        model.addAttribute("currentSortField", "txDate");
        model.addAttribute("currentSortDir", "desc");

        // xoa cac param khong can thiet
        allParams.remove("page");
        allParams.remove("size");

        // 3. Gửi lại các tham số user đang nhập (để giữ trạng thái form)
        model.addAttribute("currentParams", allParams);
        model.addAttribute("filters", allParams);
        model.addAttribute("currentPage", "follow-report");

        return "pages/messages/follow-report";
    }
    @GetMapping("/follow-report/detail")
    public String transactionDetail(@RequestParam Long autoid, Model model) {

        RptLogDto detail = rptLogService.findDetailByAutoid(autoid);
        model.addAttribute("rptLog", detail);
        model.addAttribute("currentPage", "follow-report");

        return "pages/messages/follow-report-detail";
    }

    @PostMapping("/resend-pending")
    @TrackAction(ActionType.RESEND)
    public String resendPending(
            @RequestParam("orgSeqIds") List<String> orgSeqIds,
            RedirectAttributes redirectAttributes) {
        try {
            messageService.resendPending(orgSeqIds);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Resend successfully"
            );
        } catch (BusinessException be) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    be.getMessage()
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "System error"
            );
        }
        return "redirect:/messages/msg-history";
    }


}
