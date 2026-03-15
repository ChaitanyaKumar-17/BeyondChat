package com.manu.beyondchat.controller;

import com.manu.beyondchat.dto.*;
import com.manu.beyondchat.service.RegistrationService;
import com.manu.beyondchat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDto userDto){
        userService.createUser(userDto);
        return new ResponseEntity<>("User Created Succesfully",HttpStatus.CREATED);
    }

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
    public ResponseEntity<Void> completeRegistration(@RequestBody Step4Request request) {
        registrationService.processStep4AndComplete(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
