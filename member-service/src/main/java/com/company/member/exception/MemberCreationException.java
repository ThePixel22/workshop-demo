package com.company.member.exception;

public class MemberCreationException extends RuntimeException {
    public MemberCreationException(String message) {
        super(message);
    }

    public MemberCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}