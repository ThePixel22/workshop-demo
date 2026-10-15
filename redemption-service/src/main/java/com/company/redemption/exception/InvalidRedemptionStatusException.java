package com.company.redemption.exception;

public class InvalidRedemptionStatusException extends RuntimeException {
    public InvalidRedemptionStatusException(String message) {
        super(message);
    }

    public InvalidRedemptionStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}