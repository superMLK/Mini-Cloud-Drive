package com.example.miniclouddrive.controller;

import com.example.miniclouddrive.dto.request.LoginRequest;
import com.example.miniclouddrive.dto.request.RegisterRequest;
import com.example.miniclouddrive.dto.response.LoginResponse;
import com.example.miniclouddrive.exception.BusinessException;
import com.example.miniclouddrive.exception.GlobalExceptionHandler;
import com.example.miniclouddrive.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 單元測試
 * 測試認證相關 API 端點
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/register 測試")
    class RegisterTests {

        @Test
        @DisplayName("成功註冊")
        void shouldRegisterSuccessfully() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser12");
            request.setEmail("newuser@example.com");
            request.setPassword("password1");

            doNothing().when(authService).register(any(RegisterRequest.class));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rtnCode").value("0000"));

            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("註冊失敗 - 用戶名為空")
        void shouldReturnBadRequestWhenUsernameIsBlank() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("");
            request.setEmail("newuser@example.com");
            request.setPassword("password1");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("註冊失敗 - 電子郵件格式錯誤")
        void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser12");
            request.setEmail("invalid-email");
            request.setPassword("password1");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("註冊失敗 - 用戶名已存在")
        void shouldReturnBadRequestWhenUsernameExists() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existing1");
            request.setEmail("new@example.com");
            request.setPassword("password1");

            doThrow(new BusinessException("0001", "用戶名已存在"))
                    .when(authService).register(any(RegisterRequest.class));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login 測試")
    class LoginTests {

        @Test
        @DisplayName("成功登入")
        void shouldLoginSuccessfully() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password1");

            LoginResponse response = new LoginResponse("jwt-token-123", "testuser");
            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rtnCode").value("0000"))
                    .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
                    .andExpect(jsonPath("$.data.username").value("testuser"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("登入失敗 - 電子郵件為空")
        void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("");
            request.setPassword("password1");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }

        @Test
        @DisplayName("登入失敗 - 密碼錯誤")
        void shouldReturnBadRequestWhenPasswordIsWrong() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongpass1");

            doThrow(new BusinessException("0001", "無效的電子郵件或密碼"))
                    .when(authService).login(any(LoginRequest.class));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
