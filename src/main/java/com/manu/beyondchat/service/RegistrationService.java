package com.manu.beyondchat.service;

import com.manu.beyondchat.dto.*;
import com.manu.beyondchat.mapper.UserMapper;
import com.manu.beyondchat.sql.entity.UserEntity;
import com.manu.beyondchat.sql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserMapper userMapper;

    private UserRegistrationDto dto = new UserRegistrationDto();

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,}$"
    );

    public Step1Response processStep1(Step1Request request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        String generatedOtp = otpService.generateSecureOtp();
        long otpExpiryTime = otpService.setTimer();

        String message = """
        Hello,
        
        Your One-Time Password (OTP) for verification is:
        
        %s
        
        This OTP is valid for 5 minutes.
        Please do not share this code with anyone for security reasons.
        
        If you did not request this OTP, please ignore this email.
        
        Regards,
        BeyondChat
        Support Team
        """.formatted(generatedOtp);
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

    public void processStep3(Step3Request request){
        String redisKey = "registration:" + request.registrationId();
        RegistrationContext context = (RegistrationContext) redisTemplate.opsForValue().get(redisKey);

        if (context == null) {
            throw new IllegalStateException("Registration session expired. Please start over.");
        }
        if (context.getCurrentStep() < 2) {
            throw new IllegalStateException("You must verify your OTP before proceeding.");
        }

        if (request.firstName() == null || request.firstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (request.contactNumber() == null || !request.contactNumber().matches("\\d+")) {
            throw new IllegalArgumentException("Invalid contact number. Digits only.");
        }
        if (request.countryCode() == null || request.countryCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Country code is required.");
        }

        if (userRepository.existsByCountryCodeAndPhoneNumber(request.countryCode(), request.contactNumber())) {
            throw new IllegalArgumentException("This phone number is already registered to another account.");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate parsedDate = LocalDate.parse(request.dateOfBirth(), formatter);
            if (parsedDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of birth cannot be in the future.");
            }
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid date of birth format. Must be dd-mm-yyyy.");
        }

        context.setFirstName(request.firstName().trim());
        context.setLastName(request.lastName() != null ? request.lastName().trim() : null);
        context.setCountryCode(request.countryCode());
        context.setContactNumber(request.contactNumber());
        context.setDateOfBirth(request.dateOfBirth());

        context.setCurrentStep(3);

        // 5. Save back to Redis
        redisTemplate.opsForValue().set(redisKey, context, Duration.ofMinutes(60));
    }

    @Transactional
    public void processStep4AndComplete(Step4Request request){
        String redisKey = "registration:" + request.registrationId();
        RegistrationContext context = (RegistrationContext) redisTemplate.opsForValue().get(redisKey);

        if (context == null) {
            throw new IllegalStateException("Registration session expired. Please start over.");
        }
        if (context.getCurrentStep() < 3) {
            throw new IllegalStateException("You must complete previous steps before finalizing.");
        }

        if (request.username() == null || request.username().length() < 8) {
            throw new IllegalArgumentException("Username must be at least 8 characters long.");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (request.password() == null || !PASSWORD_PATTERN.matcher(request.password()).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 12 characters and contain an uppercase letter, " +
                            "lowercase letter, number, and special character."
            );
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        dto.setFirstName(context.getFirstName());
        dto.setLastName(context.getLastName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        dto.setDateOfBirth(LocalDate.parse(context.getDateOfBirth(), formatter));

        dto.setDateOfJoining(LocalDate.now());
        dto.setEmail(context.getEmail());
        dto.setCountryCode(context.getCountryCode());
        dto.setPhoneNumber(context.getContactNumber());
        dto.setUsername(request.username());
        dto.setPassword(hashedPassword);
        dto.setStatus("ONLINE");

        UserEntity user = userMapper.toEntity(dto);
        userRepository.save(user);

        redisTemplate.delete(redisKey);
    }
}
