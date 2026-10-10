package com.manu.beyondchat.domain.twofactor;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TotpService {

    public record TotpSetup(String plainSecret, String encryptedSecret, String otpauthUri) {}

    private static final String ISSUER = "BeyondChat";
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final SecureRandom secureRandom = new SecureRandom();
    private final CodeVerifier codeVerifier;
    private final SecretKeySpec encryptionKey;

    public TotpService(@Value("${spring.application.security.totp.encryption-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("TOTP encryption key must be 32 bytes, Base64-encoded.");
        }
        this.encryptionKey = new SecretKeySpec(keyBytes, "AES");

        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        verifier.setTimePeriod(30);
        verifier.setAllowedTimePeriodDiscrepancy(1); // ±1 step for clock drift
        this.codeVerifier = verifier;
    }

    /** Creates a new secret. plainSecret is for the response only; store encryptedSecret. */
    public TotpSetup generateSetup(String accountLabel) {
        String secret = secretGenerator.generate();

        QrData qrData = new QrData.Builder()
                .label(accountLabel)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        return new TotpSetup(secret, encrypt(secret), qrData.getUri());
    }

    /** Verifies a 6-digit code against an encrypted stored secret. */
    public boolean verify(String encryptedSecret, String code) {
        if (encryptedSecret == null || code == null) return false;
        String trimmed = code.trim();
        if (!trimmed.matches("\\d{6}")) return false;
        return codeVerifier.isValidCode(decrypt(encryptedSecret), trimmed);
    }

    private String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv).put(ciphertext).array();
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt TOTP secret", e);
        }
    }

    private String decrypt(String encoded) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(encoded));
            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt TOTP secret", e);
        }
    }
}