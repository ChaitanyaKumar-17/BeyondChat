package com.manu.beyondchat.common.exception;

public class AuthSessionExpiredException extends RuntimeException {
    public AuthSessionExpiredException(String message) { super(message); }
}
