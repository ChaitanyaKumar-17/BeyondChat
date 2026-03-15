package com.manu.beyondchat.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationContext implements Serializable {
    private String registrationId;
    private String email;
    private String otp;
    private long otpExpiryTime;
    private int currentStep;

    private String firstName;
    private String lastName;
    private String countryCode;
    private String contactNumber;
    private String dateOfBirth;

    public RegistrationContext(String registrationId, String email, String otp, long otpExpiryTime, int currentStep) {
        this.registrationId = registrationId;
        this.email = email;
        this.currentStep = currentStep;
        this.otp = otp;
        this.otpExpiryTime = otpExpiryTime;
    }

}
