package com.company.redemption.exception;

public class BalanceUpdateException extends RuntimeException {
    public BalanceUpdateException(String message) {
        super(message);
    }

    public BalanceUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}