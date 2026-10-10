package com.manu.beyondchat.domain.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/credentials")
    public ResponseEntity<LoginStepOneResponse> credentials(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.authenticate(request));
    }

    @PostMapping("/2fa")
    public ResponseEntity<String> verifyTwoFactor(@RequestBody TwoFactorVerifyRequest request) {
        return ResponseEntity.ok(loginService.verifyTwoFactor(request));
    }
}
