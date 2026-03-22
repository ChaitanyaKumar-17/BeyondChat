package com.manu.beyondchat.domain.registration.dto;

public record Step2VerifyRequest(String registrationId, String otp) {
}
