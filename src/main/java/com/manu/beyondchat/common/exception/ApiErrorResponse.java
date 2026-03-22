package com.manu.beyondchat.common.exception;

public record ApiErrorResponse(String error, int status, long timestamp) {
}
