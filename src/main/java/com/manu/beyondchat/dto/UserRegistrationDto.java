package com.manu.beyondchat.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserRegistrationDto {

    @NotBlank
    @Size(min = 8, max = 32)
    private String username;

    @NotBlank
    @Size(min = 12, max = 64)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 20)
    private String firstName;

    @Size(max = 20)
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(max = 15)
    private String phoneNumber;


}
