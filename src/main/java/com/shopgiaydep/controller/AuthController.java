package com.shopgiaydep.controller;

import com.shopgiaydep.dto.ApiResponse;
import com.shopgiaydep.dto.AuthRequest;
import com.shopgiaydep.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody AuthRequest request) {
        var result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", result));
    }
}
