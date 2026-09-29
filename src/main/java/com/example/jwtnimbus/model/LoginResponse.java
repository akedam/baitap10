package com.example.jwtnimbus.model;

public record LoginResponse(String token, long expiresIn) {
}
