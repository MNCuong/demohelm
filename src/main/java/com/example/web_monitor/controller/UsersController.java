package com.example.web_monitor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.web_monitor.annotation.TrackAction;
import com.example.web_monitor.model.enums.ActionType;
import com.example.web_monitor.service.ActionLogService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.web_monitor.dto.UserDto;
import com.example.web_monitor.service.UserService;

@Slf4j
@Controller
@RequestMapping("/users")
public class UsersController {

    private final UserService userService;
    private final ActionLogService actionLogService;

    public UsersController(UserService userService, ActionLogService actionLogService) {
        this.userService = userService;
        this.actionLogService = actionLogService;
    }

    @GetMapping("")
    public String index(Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "50") int size,
                        @RequestParam(defaultValue = "username") String sortField,
                        @RequestParam(defaultValue = "asc") String sortDir,
                        @RequestParam(name = "keyword", required = false) String keyword
                       ) {

        int pageNo = page - 1;
        model.addAttribute("currentPage", "users");
        String lang = LocaleContextHolder.getLocale().getLanguage();

        Page<UserDto> userPage = userService.findAllUsers(pageNo, size, sortField, sortDir, keyword, lang);

        //List<String> headers = List.of("Tên đăng nhập", "Email", "Vai trò", "Ghi chú");

        List<String> headers = List.of(
                "page.users.table.header.username",
//            "page.users.table.header.email",
                "page.users.table.header.role",
                "page.users.table.header.status"
//            "page.users.table.header.notes"
        );

        List<String> fields = List.of("username", "email", "role");
        String idField = "username";
        Map<String, String> filters = new HashMap<>();
        if (keyword != null && !keyword.isEmpty()) {
            filters.put("keyword", keyword);
        }
        filters.remove("page");
        filters.remove("size");
        filters.remove("sortField");
        filters.remove("sortDir");
        model.addAttribute("filters", filters);
        model.addAttribute("listData", userPage.getContent());
        model.addAttribute("listHeaders", headers);
        model.addAttribute("listFields", fields);
        model.addAttribute("listIdField", idField);

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentSortField", sortField);
        model.addAttribute("currentSortDir", sortDir);

        return "pages/users/list-user";
    }

    //show form add user
    @GetMapping("/add-user")
    public String getAddUserForm() {
        return "pages/users/add-user :: addUserForm";
    }

    //submit form add user
    @PostMapping("/save")
    @TrackAction(ActionType.SAVE_USER)
    public String saveNewUser(
            @Valid @ModelAttribute("userDto") UserDto userDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
        userService.saveUser(userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        return "redirect:/users";
    }

    @GetMapping("/edit/{username}")
    public String getEditUserForm(@PathVariable String username, Model model) {
        model.addAttribute("user", userService.findUserByUserName(username));
        return "pages/users/edit-user :: editUserForm";
    }

    @PostMapping("/edit")
    @TrackAction(ActionType.EDIT_USER)
    public String updateUser(UserDto user, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

//        if (bindingResult.hasErrors()) {
//            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
//            redirectAttributes.addFlashAttribute("errorMessage", msg);
//            return "redirect:/users";
//        }
        userService.editUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        return "redirect:/users";
    }

    @PostMapping("/disable/{username}")
    public String disableUser(@PathVariable String username) {
        boolean enabled = userService.isEnabled(username);
        if (enabled) {
            log.debug("Username {} has been disabled", username);
            actionLogService.disableUserAction(username);
        } else {
            actionLogService.enableUserAction(username);
        }
        return "redirect:/users";
    }

    @PostMapping("/delete/{username}")
    @TrackAction(ActionType.DELETE_USER)
    public void deleteUser(@PathVariable String username, HttpServletResponse resp) {
        userService.deleteByUsername(username);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
