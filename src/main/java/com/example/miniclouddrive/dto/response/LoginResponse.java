package com.example.miniclouddrive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    /** JWT token */
    private String token;

    /** 使用者名稱 */
    private String username;
}
