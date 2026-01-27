package com.example.miniclouddrive.dto.response;

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
public class FileExistsResponse {
    /** 已存在檔案的 ID */
    private Long existingFileId;

    /** 已存在檔案的名稱 */
    private String existingFileName;

    /** 已存在檔案的上傳時間 */
    private LocalDateTime uploadTime;
}
