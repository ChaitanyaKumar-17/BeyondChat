package com.manu.beyondchat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailVerificationDto {

    @NotBlank
    @Email
    private String email;
}
