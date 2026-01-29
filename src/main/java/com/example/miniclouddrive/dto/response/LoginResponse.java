package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登入成功回應 DTO
 */
@Data
@AllArgsConstructor
@Schema(description = "登入成功回應")
public class LoginResponse {

    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "使用者名稱", example = "testuser1")
    private String username;
}
