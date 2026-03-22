package com.manu.beyondchat.domain.registration.dto;

public record Step3Request(String registrationId,
                           String firstName,
                           String lastName,
                           String countryCode,
                           String contactNumber,
                           String dateOfBirth) {
}
