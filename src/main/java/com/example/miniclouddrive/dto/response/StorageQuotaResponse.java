package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "儲存空間不足錯誤資訊")
public class StorageQuotaResponse {

    @Schema(description = "本次上傳需要的空間（bytes）", example = "10485760")
    private Long requiredSize;

    @Schema(description = "剩餘可用空間（bytes）", example = "5242880")
    private Long remainingQuota;
}
