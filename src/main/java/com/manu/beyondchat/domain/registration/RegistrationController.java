package com.manu.beyondchat.domain.registration;

import com.manu.beyondchat.domain.auth.LoginRequest;
import com.manu.beyondchat.domain.auth.LoginService;
import com.manu.beyondchat.domain.registration.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/step1")
    public ResponseEntity<Step1Response> step1(@RequestBody Step1Request request) {
        Step1Response response = registrationService.processStep1(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/step2/verify")
    public ResponseEntity<Void> verifyOtp(@RequestBody Step2VerifyRequest request) {
        registrationService.verifyOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/step2/resend")
    public ResponseEntity<Void> resendOtp(@RequestBody ResendOtpRequest request) {
        registrationService.resendOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/step3")
    public ResponseEntity<Void> step3Details(@RequestBody Step3Request request){
        registrationService.processStep3(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/final")
    public ResponseEntity<String> completeRegistration(@RequestBody Step4Request request) {
        LoginRequest loginRequest = registrationService.processStep4AndComplete(request);
        return loginService.authenticate(loginRequest);
    }
}
