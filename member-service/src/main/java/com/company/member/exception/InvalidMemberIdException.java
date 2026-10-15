package com.company.member.exception;

public class InvalidMemberIdException extends RuntimeException {
    public InvalidMemberIdException(String message) {
        super(message);
    }
}
