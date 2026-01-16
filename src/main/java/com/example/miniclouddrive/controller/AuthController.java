package com.example.miniclouddrive.controller;

import com.example.miniclouddrive.dto.request.LoginRequest;
import com.example.miniclouddrive.dto.request.RegisterRequest;
import com.example.miniclouddrive.dto.response.ApiResponse;
import com.example.miniclouddrive.dto.response.LoginResponse;
import com.example.miniclouddrive.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /** 註冊 */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 登入 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }
}
