package com.example.web_monitor.utils;

import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.RegReportEntity;
import com.example.web_monitor.model.entities.ReGtxEntity;
import com.example.web_monitor.model.entities.StaticDataEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ParticipantUtils {
    static final List<String> msgCodes = Arrays.asList(
            "103", "198", "501", "502", "503", "504", "505", "506", "507", "509",
            "513", "518", "524", "530", "540", "542", "544", "546", "547", "548",
            "564", "565", "567", "568", "578", "586", "592", "595", "596", "598", "599",
            "900", "910", "999", "541"
    );
    static final String[][] configs = {
            {"(T6056)", "CA005"}, {"T6059", "CA001"}, {"(T6050)", "CA009"}, {"(T6055)", "CA012"},
            {"(T6054)", "CA014"}, {"(T6053)", "CA029"}, {null, "CA031"}, {null, "CA069"},
            {"(AVAION)", "DE013"}, {"(AVAION)", "DE065"}, {null, "DE083"}, {null, "DE084"},
            {"(SETTCOMP)", "CS070"}, {null, "CS071"}, {null, "CS072"}, {null, "CS075"},
            {null, "CS076"}, {"(SETTCOMP)", "CS077"}, {null, "CS078"}, {null, "CS079"},
            {null, "CS082"}, {null, "CS083"}, {null, "CS091"}, {"T6059", "RCA001"},
            {"(T6056)", "RCA005"}, {null, "RCA009"}, {null, "RCA012"}, {null, "RCA014"},
            {"(T6050)", "RCA029"}, {"(T6055)", "RCA031"}, {"(T6054)", "RCA069"},
            {"(T6053)", "RDE013"}, {null, "RDE065"}, {null, "RDE083"}, {"(AVAION)", "RDE084"},
            {"(AVAION)", "RCS070"}, {null, "RCS071"}, {null, "RCS072"}, {"(SETTCOMP)", "RCS075"},
            {null, "RCS076"}, {null, "RCS077"}, {null, "RCS078"}, {null, "RCS079"},
            {"(SETTCOMP)", "RCS082"}, {null, "RCS083"}, {null, "RCS091"}
    };


    public static List<StaticDataEntity> buildStaticData(String biccode, String usrname, String vsdcode) {
        return List.of(
                new StaticDataEntity("MICODEMAP", vsdcode, vsdcode),
                new StaticDataEntity("REFMICODE", usrname, vsdcode),
                new StaticDataEntity("REFTRADECODE", biccode, vsdcode),
                new StaticDataEntity("PARTICIPANTS", vsdcode, ""),
                new StaticDataEntity("BICCODE", biccode, "biccode")
        );
    }


    public static List<ReGtxEntity> buildRegTxList(ParticipantEntity participant) {
        return IntStream.range(0, msgCodes.size())
                .mapToObj(i -> ReGtxEntity.builder()
                        .participant(participant)
                        .vsdcode(participant.getVsdcode())
                        .msgtype("MT")
                        .msgcode(msgCodes.get(i))
                        .tltxcd(null)
                        .dmamode("D")
                        .userid(null)
                        .autotrans("A")
                        .updatetime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }


    public static List<RegReportEntity> buildReportConfigs(ParticipantEntity participant) {
        List<RegReportEntity> list = new ArrayList<>();
        for (String[] cfg : configs) {
            list.add(RegReportEntity.builder()
                    .participant(participant)
                    .vsdcode(participant.getVsdcode())
                    .eventcode(cfg[0])
                    .rptcode(cfg[1])
                    .status("Y")
                    .build());
        }
        return list;
    }


}