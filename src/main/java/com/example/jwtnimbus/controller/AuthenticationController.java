package com.example.jwtnimbus.controller;

import com.example.jwtnimbus.model.LoginRequest;
import com.example.jwtnimbus.model.LoginResponse;
import com.example.jwtnimbus.model.RegisterRequest;
import com.example.jwtnimbus.model.UserResponse;
import com.example.jwtnimbus.service.AuthenticationService;
import com.example.jwtnimbus.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    ResponseEntity<UserResponse> signup(@RequestBody RegisterRequest request) {
        var user = authenticationService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var user = authenticationService.authenticate(request);
        var token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, jwtService.getExpirationTime()));
    }
}
