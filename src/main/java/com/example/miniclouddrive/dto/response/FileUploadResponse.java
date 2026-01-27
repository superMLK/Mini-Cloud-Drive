package com.example.miniclouddrive.dto.response;

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
public class FileUploadResponse {
    /** 檔案 ID */
    private Long fileId;

    /** 檔案名稱 */
    private String fileName;

    /** 檔案大小（bytes） */
    private Long size;

    /** 上傳時間 */
    private LocalDateTime uploadTime;
}
