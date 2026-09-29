package com.example.jwtnimbus.controller;

import com.example.jwtnimbus.entity.User;
import com.example.jwtnimbus.model.UserResponse;
import com.example.jwtnimbus.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping
    ResponseEntity<List<UserResponse>> all() {
        return ResponseEntity.ok(userService.findAll().stream().map(UserResponse::from).toList());
    }
}
