package com.manu.beyondchat.domain.auth;

import com.manu.beyondchat.common.exception.AuthSessionExpiredException;
import com.manu.beyondchat.common.exception.InvalidTwoFactorCodeException;
import com.manu.beyondchat.common.exception.TooManyAttemptsException;
import com.manu.beyondchat.domain.twofactor.TotpService;
import com.manu.beyondchat.domain.user.repository.sql.UserRepository;
import com.manu.beyondchat.security.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {

    private static final String MFA_PREFIX = "login:mfa:";
    private static final String ATTEMPTS_PREFIX = "login:mfa:attempts:";
    private static final String USED_CODE_PREFIX = "totp:used:";

    private static final Duration MFA_TOKEN_TTL = Duration.ofMinutes(5);
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(15);
    private static final Duration USED_CODE_TTL = Duration.ofSeconds(90);
    private static final int MAX_2FA_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final JwtUtility jwtUtility;
    private final StringRedisTemplate stringRedisTemplate;
    private final TotpService totpService;
    private final UserRepository userRepository;

    /** Step 1: password check only. Returns a short-lived mfaToken, NOT a JWT. */
    public LoginStepOneResponse authenticate(LoginRequest request) {
        Authentication authenticatedUser = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String mfaToken = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue()
                .set(MFA_PREFIX + mfaToken, authenticatedUser.getName(), MFA_TOKEN_TTL);

        return new LoginStepOneResponse(mfaToken);
    }

    /** Step 2: verify the authenticator code and issue the real JWT. */
    public String verifyTwoFactor(TwoFactorVerifyRequest request) {
        if (request.mfaToken() == null || request.code() == null) {
            throw new AuthSessionExpiredException("Login session expired. Please log in again.");
        }

        String tokenKey = MFA_PREFIX + request.mfaToken();
        String username = stringRedisTemplate.opsForValue().get(tokenKey);
        if (username == null) {
            throw new IllegalStateException("Login session expired. Please log in again.");
        }

        // Atomic per-user attempt counter (INCR). Keyed by username, not by token,
        // so re-submitting the password for a fresh mfaToken doesn't reset the limit.
        String attemptsKey = ATTEMPTS_PREFIX + username;
        Long attempts = stringRedisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1) {
            stringRedisTemplate.expire(attemptsKey, ATTEMPT_WINDOW);
        }
        if (attempts != null && attempts > MAX_2FA_ATTEMPTS) {
            stringRedisTemplate.delete(tokenKey);
            throw new TooManyAttemptsException("Too many invalid codes. Please try again later.");
        }

        String encryptedSecret = userRepository.findTotpSecretByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Two-factor authentication is not set up for this account."));

        if (!totpService.verify(encryptedSecret, request.code())) {
            throw new InvalidTwoFactorCodeException("Invalid authenticator code.");
        }

        // Replay protection: a given code can be accepted only once within its window
        String usedKey = USED_CODE_PREFIX + username + ":" + request.code().trim();
        Boolean firstUse = stringRedisTemplate.opsForValue().setIfAbsent(usedKey, "1", USED_CODE_TTL);
        if (!Boolean.TRUE.equals(firstUse)) {
            throw new InvalidTwoFactorCodeException("This code was already used. Wait for the next one.");
        }

        stringRedisTemplate.delete(List.of(tokenKey, attemptsKey));
        return jwtUtility.generateToken(username);
    }

    /** Issues a JWT for a user who has just been verified (used by registration). */
    public String issueToken(String username) {
        return jwtUtility.generateToken(username);
    }
}