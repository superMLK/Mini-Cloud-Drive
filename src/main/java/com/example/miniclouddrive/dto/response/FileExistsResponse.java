package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 檔案已存在錯誤回應的 data 部分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "檔案已存在錯誤資訊")
public class FileExistsResponse {

    @Schema(description = "已存在檔案的 ID", example = "1")
    private Long existingFileId;

    @Schema(description = "已存在檔案的名稱", example = "document.pdf")
    private String existingFileName;

    @Schema(description = "已存在檔案的上傳時間", example = "2024-01-15T10:30:00")
    private LocalDateTime uploadTime;
}
