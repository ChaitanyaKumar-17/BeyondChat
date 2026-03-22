package com.manu.beyondchat.dto;

import java.util.List;

public record EmailRequest(EmailSender sender,
                           List<Step1Request> to,
                           String subject,
                           String textContent) {
}
