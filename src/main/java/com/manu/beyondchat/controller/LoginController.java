package com.manu.beyondchat.controller;

import com.manu.beyondchat.dto.LoginRequest;
import com.manu.beyondchat.dto.Step1Request;
import com.manu.beyondchat.dto.Step1Response;
import com.manu.beyondchat.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    public LoginService loginService;

    @PostMapping("/credentials")
    public ResponseEntity<LoginRequest> step1(@RequestBody LoginRequest request) {
        loginService.verifyUser(request);
        return ResponseEntity.ok().build();
    }
}
