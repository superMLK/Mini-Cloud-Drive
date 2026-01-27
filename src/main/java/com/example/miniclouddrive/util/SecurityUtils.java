package com.example.miniclouddrive.util;

import com.example.miniclouddrive.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具類
 * 提供從 SecurityContext 取得當前使用者資訊的靜態方法
 */
public class SecurityUtils {

    private SecurityUtils() {
        // 工具類不允許實例化
    }

    /**
     * 取得當前登入使用者的 ID
     * 
     * @return 使用者 ID
     * @throws IllegalStateException 如果使用者未登入
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 取得當前登入使用者的詳細資訊
     * 
     * @return CustomUserDetails 物件
     * @throws IllegalStateException 如果使用者未登入
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("使用者未登入");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        throw new IllegalStateException("無法取得使用者資訊");
    }

    /**
     * 檢查使用者是否已登入
     * 
     * @return true 如果已登入
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails;
    }
}
