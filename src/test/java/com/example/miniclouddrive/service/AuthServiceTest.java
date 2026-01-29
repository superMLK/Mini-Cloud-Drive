package com.example.miniclouddrive.service;

import com.example.miniclouddrive.dto.request.LoginRequest;
import com.example.miniclouddrive.dto.request.RegisterRequest;
import com.example.miniclouddrive.dto.response.LoginResponse;
import com.example.miniclouddrive.entity.User;
import com.example.miniclouddrive.exception.BusinessException;
import com.example.miniclouddrive.repository.UserRepository;
import com.example.miniclouddrive.security.CustomUserDetails;
import com.example.miniclouddrive.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService 單元測試
 * 測試註冊與登入業務邏輯
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("register 測試")
    class RegisterTests {

        @Test
        @DisplayName("成功註冊新用戶")
        void shouldRegisterSuccessfully() {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("newuser@example.com");
            request.setPassword("password123");

            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().id(1L).build());

            // When
            authService.register(request);

            // Then
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("註冊失敗 - 用戶名已存在")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");
            request.setEmail("new@example.com");
            request.setPassword("password123");

            User existingUser = User.builder().id(1L).username("existinguser").build();
            when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getRtnMsg())
                    .isEqualTo("用戶名已存在");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("註冊失敗 - 電子郵件已存在")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("existing@example.com");
            request.setPassword("password123");

            User existingUser = User.builder().id(1L).email("existing@example.com").build();
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getRtnMsg())
                    .isEqualTo("電子郵件已存在");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login 測試")
    class LoginTests {

        @Test
        @DisplayName("成功登入")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            User user = User.builder()
                    .id(1L)
                    .username("testuser")
                    .email("user@example.com")
                    .passwordHash("encodedPassword")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(jwtTokenProvider.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token-123");

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token-123");
            assertThat(response.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("登入失敗 - 密碼錯誤")
        void shouldThrowExceptionWhenPasswordIsWrong() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getRtnMsg())
                    .isEqualTo("無效的電子郵件或密碼");
        }

        @Test
        @DisplayName("登入失敗 - 用戶不存在")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("notexist@example.com");
            request.setPassword("password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getRtnMsg())
                    .isEqualTo("用戶不存在");
        }
    }
}
