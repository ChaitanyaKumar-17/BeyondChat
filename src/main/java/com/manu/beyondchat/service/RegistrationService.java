package com.manu.beyondchat.service;

import com.manu.beyondchat.dto.*;
import com.manu.beyondchat.sql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EmailService emailService;
    @Autowired
    private OtpService otpService;

    public Step1Response processStep1(Step1Request request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        String generatedOtp = otpService.generateSecureOtp();
        long otpExpiryTime = otpService.setTimer();

        String message = "Hello,\n" +
                "\n" +
                "Your One-Time Password (OTP) for verification is:\n" +
                "\n" +
                generatedOtp+"\n" +
                "\n" +
                "This OTP is valid for 5 minutes.\n" +
                "Please do not share this code with anyone for security reasons.\n" +
                "\n" +
                "If you did not request this OTP, please ignore this email.\n" +
                "\n" +
                "Regards,\n" +
                "BeyondChat\n" +
                "Support Team";
        emailService.sendEmail(request.email(),"Your One-Time Password (OTP)",message);

        String registrationId = UUID.randomUUID().toString();
        String redisKey = "registration:" + registrationId;

        RegistrationContext context = new RegistrationContext(registrationId, request.email(), generatedOtp, otpExpiryTime, 1);

        redisTemplate.opsForValue().set(redisKey, context, Duration.ofMinutes(60));

        // 5. Return only the ID to the frontend
        return new Step1Response(registrationId);
    }

    public void verifyOtp(Step2VerifyRequest request) {
        String redisKey = "registration:" + request.registrationId();

        RegistrationContext context = (RegistrationContext) redisTemplate.opsForValue().get(redisKey);

        if (context == null) {
            throw new IllegalStateException("Registration session expired or invalid. Please start over.");
        }

        if (context.getOtp() == null) {
            throw new IllegalStateException("OTP has already been verified for this session.");
        }

        if (System.currentTimeMillis() > context.getOtpExpiryTime()) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!context.getOtp().equals(request.otp())) {
            throw new IllegalArgumentException("Invalid OTP.");
        }

        context.setOtp(null);
        context.setOtpExpiryTime(0);
        context.setCurrentStep(2); // The user is now officially on Step 2 (or heading to Step 3)

        // 7. Save the updated state back to Redis, refreshing the 60-minute session TTL
        redisTemplate.opsForValue().set(redisKey, context, Duration.ofMinutes(60));
    }

    public void resendOtp(ResendOtpRequest request) {
        String redisKey = "registration:" + request.registrationId();

        RegistrationContext context = (RegistrationContext) redisTemplate.opsForValue().get(redisKey);

        if (context == null) {
            throw new IllegalStateException("Registration session expired. Please start over.");
        }

        // 1. Generate new security parameters
        String newOtp = otpService.generateSecureOtp();
        long newExpiry = otpService.setTimer();

        // 2. Update the context
        context.setOtp(newOtp);
        context.setOtpExpiryTime(newExpiry);

        // 3. Save back to Redis
        redisTemplate.opsForValue().set(redisKey, context, Duration.ofMinutes(60));

        String message = "Hello,\n" +
                "\n" +
                "Your One-Time Password (OTP) for verification is:\n" +
                "\n" +
                newOtp+"\n" +
                "\n" +
                "This OTP is valid for 5 minutes.\n" +
                "Please do not share this code with anyone for security reasons.\n" +
                "\n" +
                "If you did not request this OTP, please ignore this email.\n" +
                "\n" +
                "Regards,\n" +
                "BeyondChat\n" +
                "Support Team";
        emailService.sendEmail(context.getEmail(), "Your One-Time Password (OTP)",message);
    }

}
