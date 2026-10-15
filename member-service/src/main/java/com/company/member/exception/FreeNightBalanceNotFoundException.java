package com.company.member.exception;

public class FreeNightBalanceNotFoundException extends RuntimeException {
    public FreeNightBalanceNotFoundException(String message) {
        super(message);
    }

    public FreeNightBalanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}