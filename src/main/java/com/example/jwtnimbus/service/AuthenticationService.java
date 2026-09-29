package com.example.jwtnimbus.service;

import com.example.jwtnimbus.entity.Role;
import com.example.jwtnimbus.entity.User;
import com.example.jwtnimbus.model.LoginRequest;
import com.example.jwtnimbus.model.RegisterRequest;
import com.example.jwtnimbus.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User signup(RegisterRequest request) {
        validateRegisterRequest(request);
        var email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Email đã được sử dụng");
        }

        var user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setImage(request.image());
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public User authenticate(LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Email và mật khẩu là bắt buộc");
        }
        var email = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );
        return userRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password()) || isBlank(request.fullName())) {
            throw new IllegalArgumentException("Email, mật khẩu và họ tên là bắt buộc");
        }
        if (!request.email().contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        if (request.password().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
