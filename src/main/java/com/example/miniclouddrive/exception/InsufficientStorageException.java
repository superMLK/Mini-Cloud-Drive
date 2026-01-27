package com.example.miniclouddrive.exception;

import com.example.miniclouddrive.dto.response.StorageQuotaResponse;
import lombok.Getter;

/**
 * 儲存空間不足例外
 * 當使用者上傳檔案超過配額時拋出
 */
@Getter
public class InsufficientStorageException extends RuntimeException {
    private final Long requiredSize;
    private final Long remainingQuota;

    public InsufficientStorageException(Long requiredSize, Long remainingQuota) {
        super("儲存空間不足，需要 " + requiredSize + " bytes，剩餘 " + remainingQuota + " bytes");
        this.requiredSize = requiredSize;
        this.remainingQuota = remainingQuota;
    }

    /**
     * 轉換為回應 DTO
     */
    public StorageQuotaResponse toResponse() {
        return StorageQuotaResponse.builder()
                .requiredSize(requiredSize)
                .remainingQuota(remainingQuota)
                .build();
    }
}
