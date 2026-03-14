package com.manu.beyondchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationContext implements Serializable {
    private String registrationId;
    private String email;
    private String otp;
    private long otpExpiryTime;
    private int currentStep;

}
