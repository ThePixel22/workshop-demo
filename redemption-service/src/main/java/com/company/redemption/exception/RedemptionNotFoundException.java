package com.company.redemption.exception;

public class RedemptionNotFoundException extends RuntimeException {
    public RedemptionNotFoundException(String message) {
        super(message);
    }

    public RedemptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}