package com.example.web_monitor.controller;

import com.example.web_monitor.dto.StatisticMessageDto;
import com.example.web_monitor.dto.StatisticMessageSummaryDto;
import com.example.web_monitor.service.DashboardService;
import com.example.web_monitor.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class HomeController {
    private final MessageService messageService;
    private final DashboardService  dashboardService;
    public HomeController(MessageService messageService, DashboardService dashboardService) {
        this.messageService = messageService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String homeDashboard(
            Model model,
            HttpServletRequest request,
            HttpSession session,  // Thêm HttpSession
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "memberCode") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "", name = "keyword") String keyword,
            @RequestParam(required = false, name = "minutes") Integer minutesParam) {  // Không có defaultValue

        // XỬ LÝ MINUTES: Ưu tiên param từ request, nếu không có thì lấy từ session, cuối cùng mới là 30
        Integer minutes;

        if (minutesParam != null) {
            // Nếu có param minutes từ request (người dùng chọn mới)
            minutes = minutesParam;
            session.setAttribute("selectedMinutes", minutes); // Lưu vào session
        } else {
            // Nếu không có param, lấy từ session
            Integer sessionMinutes = (Integer) session.getAttribute("selectedMinutes");
            if (sessionMinutes != null) {
                minutes = sessionMinutes; // Lấy từ session
            } else {
                minutes = 30; // Mặc định lần đầu
            }
        }

        int pageNo = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNo, size, Sort.unsorted());

        Page<StatisticMessageDto> dashBoardPage =
                messageService.getStatisticForDashboard(pageable, keyword, minutes);

        model.addAttribute("listHeaders", List.of(
                "page.msg.table.header.memberCode",
                "page.msg.table.header.totalIn",
                "page.msg.table.header.totalOut",
                "page.msg.table.header.total"
        ));
        model.addAttribute("listFields", List.of(
                "memberCode",
                "trfCode",
                "totalIn",
                "totalOut",
                "total"
        ));
        model.addAttribute("minutes", minutes);
        model.addAttribute("listIdField", "memberCode");
        model.addAttribute("listData", dashBoardPage.getContent());

        /* ===== PAGING & SORT ===== */
        model.addAttribute("dashBoardPage", dashBoardPage);
        model.addAttribute("currentSortField", sortField);
        model.addAttribute("currentSortDir", sortDir);

        Map<String, String> allParams = new HashMap<>();
        allParams.put("keyword", keyword);
        allParams.put("minutes", String.valueOf(minutes));
        // Không remove gì cả, để giữ tất cả params
        model.addAttribute("filters", allParams);

        /* ===== MENU ACTIVE ===== */
        model.addAttribute("currentPage", "home");
        StatisticMessageSummaryDto summary = messageService.getTodayTotalStatistic();
        log.debug(summary.toString());
        model.addAttribute("summary", summary);

        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "pages/home/dashboard :: statisticContent";
        }
        return "pages/home/dashboard";
    }

}