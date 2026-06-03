package com.example.web_monitor.service;

import com.example.web_monitor.dto.RptLogDto;
import com.example.web_monitor.dto.StatusCardDTO;
import com.example.web_monitor.filter.FilterDefinition;
import com.example.web_monitor.filter.specification.GenericSpecification;
import com.example.web_monitor.mapper.RptLogMapper;
import com.example.web_monitor.model.entities.RptlogEntity;
import com.example.web_monitor.repository.RptLogRepository;
import com.example.web_monitor.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RptLogService {
    private final RptLogRepository rptLogRepository;
    private final RptLogMapper rptLogMapper;
    private final ParticipantService participantService;

    public RptLogService(RptLogRepository rptLogRepository, RptLogMapper rptLogMapper, ParticipantService participantService) {
        this.rptLogRepository = rptLogRepository;
        this.rptLogMapper = rptLogMapper;
        this.participantService = participantService;
    }

    @Transactional
    public void updateRptLog(RptlogEntity rptlog) {
        log.info("=== UPDATE RPTLOG START ===");
        log.info("ID: {}, Status: {}, ErrMsg: {}", rptlog.getAutoid(), rptlog.getStatus(), rptlog.getErrmsg());
        RptlogEntity saved = rptLogRepository.saveAndFlush(rptlog);
        log.info("Saved entity - ID: {}, Status: {}", saved.getAutoid(), saved.getStatus());
        log.info("=== UPDATE RPTLOG END ===");
    }

    public RptlogEntity findByCoreRefId(String coreRefId) {
        return rptLogRepository.findByCorerefid(coreRefId);
    }

    @Transactional
    public void deleteByCoreRefId(String coreRefId) {
        rptLogRepository.deleteAllByCorerefid(coreRefId);
    }

    @Transactional
    public void deleteByCoreRefIds(List<String> coreRefId) {
        rptLogRepository.deleteAllByCorerefidIn(coreRefId);
    }


    public List<FilterDefinition> getHistoryFilterConfig() {
        List<FilterDefinition.Option> statusOptions = List.of(FilterDefinition.Option.builder().value("0").label("filter.option.status.0").build(), FilterDefinition.Option.builder().value("1").label("filter.option.status.1").build(), FilterDefinition.Option.builder().value("2").label("filter.option.status.2").build(), FilterDefinition.Option.builder().value("3").label("filter.option.status.3").build(), FilterDefinition.Option.builder().value("E").label("filter.option.status.4").build());
        List<FilterDefinition.Option> vsdCodeOptions = participantService.getAllVsdCode().stream().map(vsd -> FilterDefinition.Option.builder().value(vsd).label(vsd).build()).toList();
        return List.of(FilterDefinition.builder().field("corerefid").label("corerefid").type(FilterDefinition.Type.TEXT).build(), FilterDefinition.builder().field("vsdcode").label("vsdcode").type(FilterDefinition.Type.SELECT).options(vsdCodeOptions).build(), FilterDefinition.builder().field("status").label("status").type(FilterDefinition.Type.SELECT).options(statusOptions).build(), FilterDefinition.builder().field("filename").label("filename").type(FilterDefinition.Type.TEXT).build(),

                // 7. Ngày giao dịch (TXDATE)
                FilterDefinition.builder().field("createdate").label("createdate").type(FilterDefinition.Type.DATE_RANGE).build());
    }

    // 2. XỬ LÝ SEARCH: Nhận Map params -> Trả về Page
    public Page<RptLogDto> search(Map<String, String> params, int page, int size) {

        Map<String, Object> filters = new HashMap<>(params);

        String today = LocalDate.now().toString();
        List<String> keys = new ArrayList<>(filters.keySet());

        for (String key : keys) {
            if (key.endsWith("From")) {
                String baseName = key.substring(0, key.length() - 4);
                String toKey = baseName + "To";

                Object fromVal = filters.get(key);
                Object toVal = filters.get(toKey);

                if (ObjectUtils.isValid(fromVal) && !ObjectUtils.isValid(toVal)) {
                    filters.put(toKey, today);
                }
            } else if (key.endsWith("To")) {
                String baseName = key.substring(0, key.length() - 2);
                String fromKey = baseName + "From";

                Object toVal = filters.get(key);
                Object fromVal = filters.get(fromKey);

                if (ObjectUtils.isValid(toVal) && !ObjectUtils.isValid(fromVal)) {
                    filters.put(fromKey, today);
                }
            }
        }

        filters.remove("page");
        filters.remove("size");

        Specification<RptlogEntity> spec = GenericSpecification.create(filters);

        return rptLogRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdate").descending())).map(rptLogMapper::toDto);
    }

    public RptLogDto findDetailByAutoid(Long autoid) {
        return rptLogRepository.findById(autoid).map(rptLogMapper::toDto).orElseThrow(() -> new RuntimeException("Report not found"));
    }

    public List<StatusCardDTO> getStatusCards(LocalDateTime txDateFrom, LocalDateTime txDateTo) {

        List<Object[]> rows;

        try {
            rows = rptLogRepository.countByStatus(txDateFrom, txDateTo);
        } catch (Exception e) {
            log.error("countByStatus failed", e);
            rows = List.of();
        }

        Map<String, Long> map = new HashMap<>();

        for (Object[] r : rows) {

            String status = "0";
            Long count = 0L;

            try {
                if (r != null && r.length > 0 && r[0] != null) {
                    status = r[0].toString();
                }

                if (r != null && r.length > 1 && r[1] != null) {
                    count = toLongSafe(r[1]);
                }
            } catch (Exception e) {
                log.warn("Invalid row in countByStatus: {}", Arrays.toString(r));
            }

            log.info("Status = {} | Total = {}", status, count);
            map.merge(status, count, Long::sum);
        }

        return List.of(new StatusCardDTO("0", "page.msg.status.init", "blue", map.getOrDefault("0", 0L)), new StatusCardDTO("1", "page.msg.status.processing", "yellow", map.getOrDefault("1", 0L)), new StatusCardDTO("2", "page.msg.status.success", "green", map.getOrDefault("2", 0L)), new StatusCardDTO("3", "page.msg.status.rejected", "orange", map.getOrDefault("3", 0L)), new StatusCardDTO("E", "page.msg.status.error", "red", map.getOrDefault("E", 0L)));
    }

    private Long toLongSafe(Object obj) {
        try {
            if (obj == null) return 0L;

            if (obj instanceof Number n) {
                return n.longValue();
            }

            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long toLong(Object o) {
        if (o == null) return 0L;
        return ((Number) o).longValue();
    }
}
