package com.example.web_monitor.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BusinessException.class, AuthorizationDeniedException.class, AccessDeniedException.class})
    public Object handleBusinessException(Exception  ex,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        
        log.warn("Business error at {}: {}", request.getRequestURI(), ex.getMessage());

        // 1. Thêm thông báo lỗi vào flash attribute
        //    File alerts-popup.js sẽ tự động đọc "errorMessage" từ body
        String message;
        if (ex instanceof BusinessException bex) {
            message = bex.getMessage();
        } else if (ex instanceof AuthorizationDeniedException || ex instanceof AccessDeniedException) {
            message = "Bạn không có quyền thực hiện chức năng này.";
        } else {
            message = "Unknown error";
        }

        // AJAX/fetch: trả JSON để frontend show popup ngay (không redirect -> HTML -> json parse error)
        if (isAjaxRequest(request)) {
            HttpStatus status = (ex instanceof BusinessException) ? HttpStatus.BAD_REQUEST : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(Map.of("message", message));
        }

        redirectAttributes.addFlashAttribute("errorMessage", message);

        // 2. Lấy URL của trang trước đó (trang có form submit)
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/"; // Nếu không biết, về trang chủ
        }

        // 3. Trả về một RedirectView để chuyển hướng trình duyệt
        return new RedirectView(referer);
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        if (xrw != null && xrw.equalsIgnoreCase("XMLHttpRequest")) return true;

        String accept = request.getHeader("Accept");
        if (accept != null && accept.toLowerCase().contains("application/json")) return true;

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) return true;

        return false;
    }

    //Lỗi hệ thống — hiển thị trang error riêng
    @ExceptionHandler(Exception.class)
    public ModelAndView handleSystemException(Exception ex, HttpServletRequest request) {
        // SYSTEM ERROR
        log.error("System error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ModelAndView mav = new ModelAndView("pages/error/system-error");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        mav.addObject("exceptionDetail", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Dữ liệu không hợp lệ");

        redirectAttributes.addFlashAttribute("errorMessage", message);

        String referer = request.getHeader("Referer");

        if (referer != null) {
            return "redirect:" + referer;
        }

        return "redirect:/";
    }


}
