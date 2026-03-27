package com.manu.beyondchat.domain.registration;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class OtpService {
    public String generateSecureOtp() {
        SecureRandom secureRandom = new SecureRandom();
        // Generates a random number between 100000 and 999999 (for 6 digit otp)
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
    public long setTimer(){
        return Instant.now()
                .plusMillis(Duration.ofMinutes(5)
                        .toMillis())
                        .toEpochMilli();
    }
}
