package com.example.web_monitor.service;

import com.example.web_monitor.dto.MemberDto;
import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.dto.RegGroupUserDto;
import com.example.web_monitor.mapper.ParticipantMapper;
import com.example.web_monitor.model.entities.*;
import com.example.web_monitor.repository.ParticipantRepository;
import com.example.web_monitor.utils.ParticipantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ParticipantService {
    private final ParticipantRepository participantRepository;
    private final ParticipantMapper participantMapper;
    private final ReGtxService reGtxService;
    private final RegTerminalService regTerminalService;
    private final RegReportSerivce regReportSerivce;
    private final RegUsersService regUsersService;
    private final StaticDataService staticDataService;
    private final RegGroupUserService regGroupUserService;

    public ParticipantService(ParticipantRepository participantRepository, ParticipantMapper participantMapper,
                              ReGtxService reGtxService, RegGroupUserService regGroupUserService, RegTerminalService regTerminalService, RegReportSerivce regReportSerivce, RegUsersService regUsersService, StaticDataService staticDataService) {
        this.participantRepository = participantRepository;
        this.participantMapper = participantMapper;
        this.regReportSerivce = regReportSerivce;
        this.regUsersService = regUsersService;
        this.staticDataService = staticDataService;
        this.reGtxService = reGtxService;
        this.regTerminalService = regTerminalService;
        this.regGroupUserService = regGroupUserService;

    }

    //Ham nay de test phan hien thi toad thong bao
    public void checkConnection() {
        boolean connectionExists = true;
        if (connectionExists) {
            throw new BusinessException(ErrorCode.CONNECTION_REFUSE);
        }
    }

    @Transactional(readOnly = true)
    public Page<ParticipantDto> findAllParticipant(int pageNo, int pageSize, String sortField, String sortDir, String keyword) {

        String dbSortField = sortField;
        log.info("sortField: {}", sortField);
        if ("role".equals(sortField)) {
            //dbSortField = "r.name";
            dbSortField = "role.name";
        }

        Sort sort = Sort.by(dbSortField);
        if ("desc".equals(sortDir)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<ParticipantEntity> participantEntity;

        if (keyword != null && !keyword.trim().isEmpty()) {
            participantEntity = participantRepository.searchParticipants(keyword.trim(), pageable);
        } else {
            participantEntity = participantRepository.findAll(pageable);
        }

        return participantEntity.map(participantMapper::toDto);
    }

    public ParticipantDto findParticipantByVsdCode(String vsdcode) {
        if (vsdcode == null) {
            throw new BusinessException(ErrorCode.VSDCODE_NOT_EXISTED);
        }
        return participantMapper.toDto(participantRepository.findByVsdcode(vsdcode));
    }

    public ParticipantDto getParticipantByVsdCode(String vsdCode) {
        if (vsdCode == null) {
            throw new BusinessException(ErrorCode.VSDCODE_NOT_EXISTED);
        }
        ParticipantDto participantDto = participantMapper.toDto(participantRepository.findByVsdcode(vsdCode));
        if (participantDto == null) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_EXISTED);
        }
        return participantDto;
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public void saveParticipant(ParticipantDto participantDto) {
        List<ParticipantEntity> participantEntityList = participantRepository.findExisting(
                participantDto.getVsdcode(), participantDto.getBiccode(), participantDto.getShortname());
        validateBicCodeAndVsdCode(participantDto);
        for (ParticipantEntity participantEntity : participantEntityList) {
            if (participantEntity.getVsdcode().equals(participantDto.getVsdcode())) {
                throw new BusinessException(ErrorCode.VSDCODE_EXISTED);
            }
            if (participantEntity.getBiccode().equals(participantDto.getBiccode())) {
                throw new BusinessException(ErrorCode.BICCODE_EXISTED);
            }
            if (participantEntity.getShortname().equals(participantDto.getShortname())) {
                throw new BusinessException(ErrorCode.SHORTNAME_EXISTED);
            }
        }
// PARTICIPANTS
        ParticipantEntity participant = participantRepository.save(
                ParticipantEntity.builder()
                        .vsdcode(participantDto.getVsdcode())
                        .biccode(participantDto.getBiccode())
                        .shortname(participantDto.getShortname())
                        .fullnameen(participantDto.getFullnameen())
                        .fullnamevn(participantDto.getFullnamevn())
                        .status("A")
                        .consumers(1)
                        .userid(null)
                        .groupid(null)
                        .usertype("MSP")
                        .updatetime(LocalDateTime.now())
                        .groupqueue(participantDto.getGroupid())
                        .build()
        );
        regUsersService.save(
                RegUserEntity.builder()
                        .participant(participant)  // gán entity
                        .usrname(participantDto.getUsrname())
                        .vsdcode(participantDto.getVsdcode())
                        .authmode("I")
                        .usrpwd("123@abcd")
                        .tokenid("")
                        .userid(null)
                        .status("Y")
                        .updatetime(LocalDateTime.now())
                        .role(false)
                        .grid(participantDto.getGroupid())
                        .build()
        );


        regTerminalService.save(
                RegTerminalEntity.builder()
                        .participant(participant) // gán participant
                        .vsdcode(participantDto.getVsdcode())
                        .ipaddr(participantDto.getIpAddr())
                        .updatetime(LocalDateTime.now())
                        .status("Y")
                        .build()
        );

        List<StaticDataEntity> staticDataList = ParticipantUtils.buildStaticData(
                participantDto.getBiccode(),
                participantDto.getUsrname(),
                participantDto.getVsdcode()
        );

        staticDataService.saveAll(staticDataList);


        List<ReGtxEntity> regTxList = ParticipantUtils.buildRegTxList(participant);
        reGtxService.saveAll(regTxList);

        List<RegReportEntity> regReportList = ParticipantUtils.buildReportConfigs(participant);
        regReportSerivce.saveAll(regReportList);

    }


    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public void editParticipant(ParticipantDto participantDto) {
        Optional<ParticipantEntity> optionalParticipant = participantRepository.findById(participantDto.getId());

        if (optionalParticipant.isEmpty()) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_EXISTED);
        }
        ParticipantEntity participant = optionalParticipant.get();
        validateVsdCode(participantDto, participant);
        if(participantDto.getBiccode().length() !=8 ||participantDto.getBiccode()==null){
            throw new BusinessException(ErrorCode.BICCODE_INVALID_LENGTH);
        }
        participant.setGroupqueue(participantDto.getGroupqueue());
        participant.setStatus(participantDto.getStatus());
        participant.setFullnameen(participantDto.getFullnameen());
        participant.setShortname(participantDto.getShortname());
        participant.setFullnamevn(participantDto.getFullnamevn());
        updateRelatedTables(participant, participantDto);
        participant.setBiccode(participantDto.getBiccode());
        participant.setVsdcode(participantDto.getVsdcode());
        participantRepository.save(participant);
    }


    public List<RegGroupUserDto> getAllGroupUser() {
        List<RegGroupUserEntity> list = regGroupUserService.getAllGroupUser();

        return list.stream()
                .map(e -> new RegGroupUserDto(
                        e.getGrid(),
                        e.getGrname()
                ))
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public void deleteParticipant(Long id) {
        try {
            Optional<ParticipantEntity> participantEntity = participantRepository.findById(id);
            staticDataService.deleteByVsdCode(participantEntity.get().getVsdcode(), participantEntity.get().getBiccode());
            participantRepository.delete(participantEntity.get());
        } catch (BusinessException e) {
            throw new BusinessException(e.getErrorCode());
        }
    }

    private void validateVsdCode(ParticipantDto participantDto, ParticipantEntity participant) {
        if (participantDto.getVsdcode().length() != 3 || !participantDto.getVsdcode().matches("\\d{3}")) {
            throw new BusinessException(ErrorCode.VSDCODE_INVALID_LENGTH);
        }
        if (participantRepository.existsByVsdcodeAndIdNot(participantDto.getVsdcode(), participant.getId())) {
            throw new BusinessException(ErrorCode.VSDCODE_EXISTED);
        }
        if (participantRepository.existsByBiccodeAndIdNot(participantDto.getBiccode(), participant.getId())) {
            throw new BusinessException(ErrorCode.BICCODE_EXISTED);
        }
    }

    private void validateBicCodeAndVsdCode(ParticipantDto dto) {

        if (dto.getBiccode().length() != 8) {
            throw new BusinessException(ErrorCode.BICCODE_INVALID_LENGTH);
        }
        String vsdCode = dto.getVsdcode();
        String bicCode = dto.getBiccode();
        if (vsdCode == null && vsdCode.length() != 3) {
            throw new BusinessException(ErrorCode.VSDCODE_INVALID_LENGTH);
        }
        StringBuilder expected = new StringBuilder("VSDC").append(vsdCode);
        while (expected.length() < 8) {
            expected.append("X");
        }
        String expectedBiccode = expected.toString();
        if (!expectedBiccode.equalsIgnoreCase(bicCode)) {
            throw new BusinessException(
                    ErrorCode.BICCODE_INVALID
            );
        }
    }

    private void updateRelatedTables(ParticipantEntity participantEntity, ParticipantDto participantDto) {
        regTerminalService.updateVsdCode(participantEntity, participantDto);
        reGtxService.updateVsdCode(participantEntity, participantDto);
        regReportSerivce.updateVsdCode(participantEntity, participantDto);
        staticDataService.updateDataCapAndDataVal(participantEntity, participantDto);
        regUsersService.updateVsdCode(participantEntity, participantDto);

    }
public List<String> getAllBiccode(){
        return participantRepository.getAllBiccode();
}public List<String> getAllVsdCode(){
        return participantRepository.getAllVsdCode();
}

    public List<MemberDto> getListMember() {
        List<String> excludes = List.of("VSDSVN01", "VSDSVN06", "VSDSVN09");

        return participantRepository
                .findByBiccodeNotInOrderByVsdcode(excludes)
                .stream()
                .map(p -> new MemberDto(p.getVsdcode(), p.getFullName(), p.getShortname()))
                .toList();
    }
    public ParticipantEntity findParticipantByBicCode(String bicCode){
        return participantRepository.findByBiccode(bicCode);
    }

}