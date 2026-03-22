package com.manu.beyondchat.integration.email;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface BrevoHttpClient {

    @PostExchange("/smtp/email")
    void sendEmail(@RequestBody EmailRequest request);
}
