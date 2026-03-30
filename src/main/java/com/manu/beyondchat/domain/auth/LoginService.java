package com.manu.beyondchat.domain.auth;

import com.manu.beyondchat.security.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtility jwtUtility;

    public ResponseEntity<String> authenticate(LoginRequest request){
        Authentication authenticatedUser = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String jwtToken = jwtUtility.generateToken(authenticatedUser.getName());
        return ResponseEntity.ok(jwtToken); // returns the JWT tokens
    }
}
