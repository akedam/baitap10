package com.example.jwtnimbus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/login"})
    String login() {
        return "login";
    }

    @GetMapping("/profile")
    String profile() {
        return "profile";
    }
}
