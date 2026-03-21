package com.manu.beyondchat.dto;

public record ApiErrorResponse(String error, int status, long timestamp) {
}
