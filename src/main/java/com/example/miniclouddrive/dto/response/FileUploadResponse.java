package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 檔案上傳成功回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "檔案上傳成功回應")
public class FileUploadResponse {

    @Schema(description = "檔案 ID", example = "1")
    private Long fileId;

    @Schema(description = "檔案名稱", example = "document.pdf")
    private String fileName;

    @Schema(description = "檔案大小（bytes）", example = "1048576")
    private Long size;

    @Schema(description = "上傳時間", example = "2024-01-15T10:30:00")
    private LocalDateTime uploadTime;
}
