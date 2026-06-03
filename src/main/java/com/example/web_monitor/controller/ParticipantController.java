package com.example.web_monitor.controller;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.web_monitor.service.ParticipantService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/participants")
public class ParticipantController {

    private final ParticipantService participantService;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplate;
    private final MessageSource messageSource;
    @Value("${session.timeout.seconds}")
    private long sessionTimeoutSeconds;// 30p

    public ParticipantController(MessageSource messageSource, ParticipantService participantService, RedisService redisService, RedisTemplate<String, String> redisTemplate) {
        this.participantService = participantService;
        this.redisService = redisService;
        this.redisTemplate = redisTemplate;
        this.messageSource = messageSource;
    }

    @GetMapping("list-participants")
    public String listParticipants(Model model,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "50") int size,
                                   @RequestParam(defaultValue = "vsdcode") String sortField,
                                   @RequestParam(defaultValue = "asc") String sortDir,
                                   @RequestParam(name = "keyword", required = false) String keyword) {
        int pageNo = page - 1;

        Page<ParticipantDto> participantPage = participantService.findAllParticipant(pageNo, size, sortField, sortDir, keyword);
        List<String> headers = List.of(
                "page.participants.table.header.vsdcode",
                "page.participants.table.header.biccode",
                "page.participants.table.header.shortname",
                "page.participants.table.header.fullnamevn",
                "page.participants.table.header.usertype",
                "page.acknak.table.header.updateDate",
                "page.participants.table.header.status"
        );

        List<String> fields = List.of("vsdcode", "biccode", "shortname", "fullnamevn", "usertype","updatetime", "status");
        String idField = "vsdcode";
        Map<String, String> filters = new HashMap<>();
        if (keyword != null && !keyword.isEmpty()) {
            filters.put("keyword", keyword);
        }
        filters.remove("page");
        filters.remove("size");
        filters.remove("sortField");
        filters.remove("sortDir");
        model.addAttribute("filters", filters);
        model.addAttribute("listData", participantPage.getContent());
        model.addAttribute("listHeaders", headers);
        model.addAttribute("listFields", fields);
        model.addAttribute("listIdField", idField);

        model.addAttribute("participantPage", participantPage);
        model.addAttribute("currentSortField", sortField);
        model.addAttribute("currentSortDir", sortDir);

        model.addAttribute("keyword", keyword);

        model.addAttribute("currentPage", "participants");
        return "pages/participants/list-participant";
    }

    @GetMapping("/member-online")
    public String getMemberOnline(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search
    ) {

        Page<String> memberKeys = redisService.scanClientLoginKeysAsPage(page, size, search);

        int startIndex = (page - 1) * size;

        List<Map<String, Object>> memberData = new ArrayList<>();
        for (int i = 0; i < memberKeys.getContent().size(); i++) {
            String key = memberKeys.getContent().get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("index", startIndex + i + 1);
            map.put("user", key);

            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            Instant now = Instant.now();
            Instant lastActivityTime = null;

            if (ttlSeconds != null && ttlSeconds > 0) {
                long lastActivityAgo = sessionTimeoutSeconds - ttlSeconds;
                lastActivityTime = now.minusSeconds(lastActivityAgo);
            }

            String lastMessageTime = lastActivityTime == null ? "" :
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.systemDefault())
                            .format(lastActivityTime);

            map.put("lastMessageTime", lastMessageTime);
            map.put("action", key);
            memberData.add(map);
        }
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", search != null ? search : "");

        List<String> headers = List.of(
                "header.index",
                "header.user",
                "header.lastMessageTime",
                "header.action"
        );

        List<String> fields = List.of("index", "user", "lastMessageTime", "action"); // thêm index vào

        model.addAttribute("listHeaders", headers);
        model.addAttribute("listFields", fields);
        model.addAttribute("listData", memberData);
        model.addAttribute("listIdField", null);
        model.addAttribute("currentSortField", "");
        model.addAttribute("currentSortDir", "");
        model.addAttribute("memberOnlinePage", memberKeys);
        model.addAttribute("filters", filters);
        model.addAttribute("currentPage", "member-online");


        return "pages/participants/member-online";
    }


    @PostMapping("/member-online/kick")
    @TrackAction(ActionType.KICK_PARTICIPANT)
    @ResponseBody
    public Map<String, Object> kickMember(@RequestBody Map<String, String> payload) {
        String clientKey = payload.get("client"); // Lấy đúng key từ JSON
        boolean deleted = redisService.kickParticipant(clientKey);

        Map<String, Object> result = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();

        if (deleted) {
            String msg = messageSource.getMessage("participant.kick.success", null, locale);
            result.put("success", true);
            result.put("message", msg);
        } else {
            String msg = messageSource.getMessage("participant.kick.fail", null, locale);
            result.put("success", false);
            result.put("message", msg);
        }
        return result;
    }
  @PostMapping("/banned-participant/kick")
    @TrackAction(ActionType.KICK_PARTICIPANT)
    @ResponseBody
    public Map<String, Object> activeParticipant(@RequestBody Map<String, String> payload) {
        String clientKey = payload.get("client"); // Lấy đúng key từ JSON
        boolean deleted = redisService.activeParticipant(clientKey);

        Map<String, Object> result = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();

        if (deleted) {
            String msg = messageSource.getMessage("participant.active.success", null, locale);
            result.put("success", true);
            result.put("message", msg);
        } else {
            String msg = messageSource.getMessage("participant.active.fail", null, locale);
            result.put("success", false);
            result.put("message", msg);
        }
        return result;
    }

    @GetMapping("/add-participant")
    public String addParticipantForm(ParticipantDto participantDto, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("groupUsers", participantService.getAllGroupUser());
        return "pages/participants/add-participant :: addParticipantForm";
    }
    @GetMapping("/banned-participant")
    public String getMemberBanned(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search
    ) {

        Page<String> memberKeys = redisService.scanClientBannedKeysAsPage(page, size, search);

        int startIndex = (page - 1) * size;

        List<Map<String, Object>> memberData = new ArrayList<>();
        for (int i = 0; i < memberKeys.getContent().size(); i++) {
            String key = memberKeys.getContent().get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("index", startIndex + i + 1);
            map.put("user", key);

            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            map.put("ttlSeconds", ttlSeconds);
            map.put("action", key);
            memberData.add(map);
        }
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", search != null ? search : "");

        List<String> headers = List.of(
                "header.index",
                "header.user",
                "header.ttlSeconds",
                "header.action"
        );

        List<String> fields = List.of("index", "user", "ttlSeconds", "action");

        model.addAttribute("listHeaders", headers);
        model.addAttribute("listFields", fields);
        model.addAttribute("listData", memberData);
        model.addAttribute("listIdField", null);
        model.addAttribute("currentSortField", "");
        model.addAttribute("currentSortDir", "");
        model.addAttribute("bannedParticipantPage", memberKeys);
        model.addAttribute("filters", filters);
        model.addAttribute("currentPage", "banned-participant");


        return "pages/participants/banned-participant";
    }

    @GetMapping("/edit-participant/{vsdcode}")
    public String getEditParticipantForm(@PathVariable String vsdcode, Model model) {
        model.addAttribute("participant", participantService.findParticipantByVsdCode(vsdcode));
        model.addAttribute("groupUsers", participantService.getAllGroupUser());
        return "pages/participants/edit-participant :: editParticipantForm";
    }

    @PostMapping("/edit-participant")
    @TrackAction(ActionType.EDIT_PARTICIPANT)
    public String editParticipant(ParticipantDto participantDto, RedirectAttributes redirectAttributes) {
        participantService.editParticipant(participantDto);
        redirectAttributes.addFlashAttribute("successMessage", "Participant edited successfully");
        return "redirect:/participants/list-participants";
    }

    @PostMapping("/delete-participant/{id}")
    @TrackAction(ActionType.DELETE_PARTICIPANT)
    public void deleteParticipant(@PathVariable Long id, HttpServletResponse resp) {
        participantService.deleteParticipant(id);
        resp.setStatus(HttpServletResponse.SC_OK); // 200
    }


    @PostMapping("/check-connection")
    public String checkConnection(Model model, RedirectAttributes redirectAttributes) {

        participantService.checkConnection();
        // Thành công
        redirectAttributes.addFlashAttribute("successMessage", "The new connection has been created successfully!");
        return "redirect:/participants/member-online";
    }

    @GetMapping("/view-participant-detail/{vsdcode}")
    public String viewParticipantDetail(Model model, @PathVariable String vsdcode) {
        ParticipantDto participantDto = participantService.getParticipantByVsdCode(vsdcode);
        model.addAttribute("participant", participantDto);
        return "pages/participants/view-participant-detail";
    }

    @PostMapping("/save")
    @TrackAction(ActionType.SAVE_PARTICIPANT)
    public String save(@Valid @ModelAttribute ParticipantDto participantDto, BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/participants/list-participants";
        }
        participantService.saveParticipant(participantDto);
        redirectAttributes.addFlashAttribute("successMessage", "New participant added successfully!");
        return "redirect:/participants/list-participants";
    }

    @PostMapping("/edit")
    @TrackAction(ActionType.EDIT_PARTICIPANT)
    public String edit(@Valid @ModelAttribute ParticipantDto participantDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/list-participants";
        }
        participantService.editParticipant(participantDto);
        return "redirect:/list-participants";
    }

}
