package com.example.web_monitor.service;

import com.example.web_monitor.dto.AckNakDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.filter.FilterDefinition;
import com.example.web_monitor.mapper.AckNakMapper;
import com.example.web_monitor.model.entities.AckNakEntity;
import com.example.web_monitor.repository.AckNakRepository;
import com.example.web_monitor.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
public class AckNakService {
    private final AckNakRepository ackNakRepository;
    private final AckNakMapper ackNakMapper;
    private final ParticipantService participantService;

    public AckNakService(AckNakRepository ackNakRepository, AckNakMapper ackNakMapper, ParticipantService participantService) {
        this.ackNakRepository = ackNakRepository;
        this.ackNakMapper = ackNakMapper;
        this.participantService = participantService;
    }

    public List<FilterDefinition> getHistoryFilterConfigAckNak() {
        List<FilterDefinition.Option> statusOptions = List.of(FilterDefinition.Option.builder().value("S").label("acknak.status.S").build(), FilterDefinition.Option.builder().value("N").label("acknak.status.N").build());
        List<FilterDefinition.Option> senderOptions = participantService.getListMember().stream().map(m -> FilterDefinition.Option.builder().value(m.getVsdCode() + " - " + m.getShortname()).label(m.getFullName()).build()).toList();

        return List.of(FilterDefinition.builder().field("refSeqId").label("refSeqId").type(FilterDefinition.Type.TEXT).build(), FilterDefinition.builder().field("sendingPlace").label("sendingPlace").type(FilterDefinition.Type.SELECT).options(senderOptions).build(),  FilterDefinition.builder().field("txDate").label("txDate").type(FilterDefinition.Type.DATE_RANGE).build(), FilterDefinition.builder().field("status").label("status").type(FilterDefinition.Type.SELECT).options(statusOptions).build()

        );
    }

    public Page<AckNakDto> searchMessagesAckNak(Map<String, String> params, int page, int size) {
        String sendingPlace = ObjectUtils.isValid(params.get("sendingPlace")) ? params.get("sendingPlace").trim() : null;
        if (sendingPlace != null && sendingPlace.contains("-")) {
            sendingPlace = sendingPlace.substring(sendingPlace.indexOf("-") + 1).trim();
        }
        String refSeqId = ObjectUtils.isValid(params.get("refSeqId")) ? params.get("refSeqId") : null;
        String status = ObjectUtils.isValid(params.get("status")) ? params.get("status") : null;

        LocalDateTime txDateFrom = null;
        LocalDateTime txDateTo = null;

        if (ObjectUtils.isValid(params.get("txDateFrom"))) {
            String fromStr = params.get("txDateFrom").trim();
            if (!fromStr.isEmpty()) {
                txDateFrom = LocalDate.parse(fromStr).atStartOfDay();
            }
        }

        if (ObjectUtils.isValid(params.get("txDateTo"))) {
            String toStr = params.get("txDateTo").trim();
            if (!toStr.isEmpty()) {
                txDateTo = LocalDate.parse(toStr).atTime(23, 59, 59);
            }
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        return ackNakRepository.searchAckNakWithParticipant(sendingPlace, refSeqId, status, txDateFrom, txDateTo, pageable);
    }


    public AckNakDto.Detail getDetail(Long autoid) {
        AckNakDto.Detail ackNakDto = ackNakRepository.findByAutoid(autoid);
        if (ackNakDto == null) {
            throw new BusinessException(ErrorCode.ACKNAK_NOT_EXISTED);
        }
        return ackNakDto;
    }
    public Optional<AckNakEntity> getAckNak(String seqId, String biccode){
        return ackNakRepository.findFirstBySeqidAndBiccodeOrderByAutoidDesc(seqId,biccode);
    }
}
