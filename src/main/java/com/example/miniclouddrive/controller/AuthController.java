package com.example.miniclouddrive.controller;

import com.example.miniclouddrive.dto.request.LoginRequest;
import com.example.miniclouddrive.dto.request.RegisterRequest;
import com.example.miniclouddrive.dto.response.ApiResponseCode;
import com.example.miniclouddrive.dto.response.LoginResponse;
import com.example.miniclouddrive.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證 API
 * 處理用戶註冊與登入
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "認證 API", description = "用戶註冊與登入相關操作")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "用戶註冊", description = "註冊新用戶帳號，用戶名與電子郵件不可重複")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "註冊成功"),
            @ApiResponse(responseCode = "400", description = "請求驗證失敗或用戶名/電子郵件已存在", content = @Content(schema = @Schema(implementation = ApiResponseCode.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseCode<Void>> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponseCode.success());
    }

    @Operation(summary = "用戶登入", description = "使用電子郵件與密碼進行登入，成功後返回 JWT Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登入成功，返回 JWT Token"),
            @ApiResponse(responseCode = "400", description = "電子郵件或密碼錯誤", content = @Content(schema = @Schema(implementation = ApiResponseCode.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseCode<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponseCode.success(loginResponse));
    }
}
