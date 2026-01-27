package com.example.miniclouddrive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 檔案上傳請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    /** 上傳的檔案（必填） */
    private MultipartFile file;

    /** 目標資料夾 ID（選填，null 表示上傳到根目錄） */
    private Long folderId;

    /**
     * 重複檔案處理方式（選填）：
     * - null: 拒絕上傳，回傳錯誤讓前端顯示選項
     * - 0: 覆蓋現有檔案
     * - 1: 自動加後綴（如 file(1).pdf）
     */
    private Integer duplicateAction;
}
