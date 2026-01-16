package com.example.miniclouddrive.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 註冊 Request DTO
 */
@Data
public class RegisterRequest {
    @Size(min = 8, max = 12, message = "使用者名稱長度必須在8到12個字元之間")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "使用者名稱只能包含英文字母和數字")
    @NotBlank(message = "使用者名稱不能為空")
    private String username;

    @Email(message = "電子郵件格式不正確")
    @NotBlank(message = "電子郵件不能為空")
    private String email;

    @Size(min = 8, max = 12, message = "密碼長度必須在8到12個字元之間")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "密碼只能包含英文字母和數字")
    @NotBlank(message = "密碼不能為空")
    private String password;
}
