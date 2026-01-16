package com.example.miniclouddrive.service;

import com.example.miniclouddrive.dto.request.LoginRequest;
import com.example.miniclouddrive.dto.request.RegisterRequest;
import com.example.miniclouddrive.dto.response.ApiReturnCode;
import com.example.miniclouddrive.dto.response.LoginResponse;
import com.example.miniclouddrive.entity.User;
import com.example.miniclouddrive.exception.BusinessException;
import com.example.miniclouddrive.repository.UserRepository;
import com.example.miniclouddrive.security.CustomUserDetails;
import com.example.miniclouddrive.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequest request) {
        // 檢查用戶名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ApiReturnCode.INVALID_PARAM.getCode(), "用戶名已存在");
        }

        // 檢查email是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ApiReturnCode.INVALID_PARAM.getCode(), "電子郵件已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new BusinessException(ApiReturnCode.INVALID_PARAM.getCode(), "無效的電子郵件或密碼");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ApiReturnCode.INVALID_PARAM.getCode(), "用戶不存在"));

        CustomUserDetails userDetails = CustomUserDetails.from(user);

        String token = jwtTokenProvider.generateToken(userDetails);

        return new LoginResponse(token, user.getUsername());
    }
}
