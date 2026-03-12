package com.cmj.risk.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(40000, "Bad request"),
    UNAUTHORIZED(40100, "Unauthorized"),
    FORBIDDEN(40300, "Forbidden"),
    NOT_FOUND(40400, "Resource not found"),
    CONFLICT(40900, "Resource conflict"),
    INVALID_CREDENTIALS(41001, "Username or password is incorrect"),
    ACCOUNT_DISABLED(41002, "Account is disabled"),
    SYSTEM_ERROR(50000, "System error"),
    FEATURE_DISABLED(50100, "Feature is not enabled in this phase");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
