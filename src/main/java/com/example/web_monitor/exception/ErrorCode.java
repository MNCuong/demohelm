package com.example.web_monitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1003, "Email existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1004, "User not existed", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1005, "Roles not existed", HttpStatus.BAD_REQUEST),
    MISSING_INFOMATION(1006, "Missing information", HttpStatus.BAD_REQUEST),
    VSDCODE_NOT_EXISTED(1007, "Vsdcode not existed", HttpStatus.BAD_REQUEST),
    PARTICIPANT_NOT_EXISTED(1008, "Participant not existed", HttpStatus.BAD_REQUEST),
    VSDCODE_EXISTED(1009, "Vsdcode existed", HttpStatus.BAD_REQUEST),
    BICCODE_EXISTED(10010, "Biccode existed", HttpStatus.BAD_REQUEST),
    SHORTNAME_EXISTED(1011, "Shortname existed", HttpStatus.BAD_REQUEST),
    BICCODE_INVALID(1012, "Invalid BICCODE: must follow the format 'VSDC' + VSDCODE + 'X' padding to total 8 characters.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1013, "Password is invalid: must be at least 8 characters, contain letters, numbers, and special characters", HttpStatus.BAD_REQUEST),
    DATA_NOT_FOUND(1014, "Data not found", HttpStatus.BAD_REQUEST),
    ACKNAK_NOT_EXISTED(1015, "Acknak not existed", HttpStatus.BAD_REQUEST),
    VSDCODE_INVALID_LENGTH(1016, "VSDCode must be exactly 3 numbers", HttpStatus.BAD_REQUEST),
    BICCODE_INVALID_LENGTH(1017, "BICCode must be exactly 8 characters", HttpStatus.BAD_REQUEST),
    RESEND_OUT_MESSAGE_FORBIDDEN(1018, "Resend is not allowed for output (OUT) or response messages", HttpStatus.BAD_REQUEST),
    CONNECTION_REFUSE(2003, "Unauthenticated", HttpStatus.BAD_REQUEST),
    SOME_MESSAGE_NOT_ELIGIBLE_FOR_RESEND(1019, "One or more selected messages are not eligible for resend", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(4004, "You do not have permission", HttpStatus.FORBIDDEN),
    HOST_TIMEOUT(504, "Target service did not respond within timeout", HttpStatus.GATEWAY_TIMEOUT),
    HOST_RESPONSE_INVALID(502, "Invalid response from target service", HttpStatus.BAD_GATEWAY),
    HOST_UNREACHABLE(503, "Target service is unreachable", HttpStatus.SERVICE_UNAVAILABLE);    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
