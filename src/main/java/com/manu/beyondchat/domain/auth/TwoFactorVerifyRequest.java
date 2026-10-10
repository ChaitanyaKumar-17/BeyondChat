package com.manu.beyondchat.domain.auth;

public record TwoFactorVerifyRequest(String mfaToken, String code) {
}
