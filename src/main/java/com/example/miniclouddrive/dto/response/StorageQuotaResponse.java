package com.example.miniclouddrive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 儲存空間不足錯誤回應的 data 部分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageQuotaResponse {
    /** 本次上傳需要的空間（bytes） */
    private Long requiredSize;

    /** 剩餘可用空間（bytes） */
    private Long remainingQuota;
}
