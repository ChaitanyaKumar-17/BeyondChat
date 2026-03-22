package com.manu.beyondchat.integration.email;

import com.manu.beyondchat.domain.registration.dto.Step1Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final BrevoHttpClient brevoClient;

    public void sendNotification(String recipientEmail, String subject, String content) {
        // Hardcoded system sender for consistency across the app
        EmailSender sender = new EmailSender("BeyondChat System", "chaitanya17008@gmail.com");
        List<Step1Request> to = List.of(new Step1Request(recipientEmail));

        EmailRequest request = new EmailRequest(sender, to, subject, content);

        brevoClient.sendEmail(request);
        System.out.println("Success: Email dispatched via Brevo HTTP API.");

    }
}
