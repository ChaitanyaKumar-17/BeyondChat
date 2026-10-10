package com.manu.beyondchat.common.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {

        log.warn("Bad request - Validation failed: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException ex) {

        log.warn("State conflict: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {

        log.warn("Failed login attempt: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                "Invalid username or password",
                HttpStatus.UNAUTHORIZED.value(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {

        log.warn("Authentication failed: {}", ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Invalid credentials",
                HttpStatus.UNAUTHORIZED.value(),
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalApiErrors(RestClientResponseException ex) {

        log.error("Brevo API Error: {}", ex.getResponseBodyAsString());

        ApiErrorResponse response = new ApiErrorResponse(
                "External Integration Error: The email delivery service rejected the request.",
                ex.getStatusCode().value(),
                System.currentTimeMillis()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleNetworkTimeouts(ResourceAccessException ex) {

        log.error("Network Error: Could not reach Brevo. {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                "Service Unavailable: Email service is currently unreachable due to network latency.",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);

        ApiErrorResponse response = new ApiErrorResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {

        log.warn("JWT expired: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                "Your session has expired. Please log in again.",
                HttpStatus.UNAUTHORIZED.value(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(JwtException ex) {

        log.warn("Invalid JWT token: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                "Invalid authentication token.",
                HttpStatus.UNAUTHORIZED.value(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({AuthSessionExpiredException.class, InvalidTwoFactorCodeException.class})
    public ResponseEntity<ApiErrorResponse> handleTwoFactorAuthFailures(RuntimeException ex) {
        log.warn("2FA auth failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(
                ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), System.currentTimeMillis()));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ApiErrorResponse> handleTooManyAttempts(TooManyAttemptsException ex) {
        log.warn("Rate limit hit: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiErrorResponse(
                ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value(), System.currentTimeMillis()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "Malformed or missing request body.", HttpStatus.BAD_REQUEST.value(), System.currentTimeMillis()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {

        log.error("Unhandled exception: ", ex);

        ApiErrorResponse response = new ApiErrorResponse(
                "An unexpected internal server error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
