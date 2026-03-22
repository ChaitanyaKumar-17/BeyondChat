package com.manu.beyondchat.integration.email;

import com.manu.beyondchat.domain.registration.dto.Step1Request;

import java.util.List;

public record EmailRequest(EmailSender sender,
                           List<Step1Request> to,
                           String subject,
                           String textContent) {
}
