package com.shopgiaydep.service;

import com.shopgiaydep.dto.AuthRequest;
import com.shopgiaydep.entity.Admin;
import com.shopgiaydep.repository.AdminRepository;
import com.shopgiaydep.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(AuthRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai username hoặc password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Sai username hoặc password");
        }

        String token = jwtUtil.generateToken(admin.getUsername());

        return Map.of(
            "token", token,
            "username", admin.getUsername(),
            "email", admin.getEmail(),
            "fullName", admin.getFullName() != null ? admin.getFullName() : ""
        );
    }
}
