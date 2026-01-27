package com.example.miniclouddrive.controller;

import com.example.miniclouddrive.dto.request.FileUploadRequest;
import com.example.miniclouddrive.dto.response.ApiResponse;
import com.example.miniclouddrive.dto.response.FileUploadResponse;
import com.example.miniclouddrive.service.FileService;
import com.example.miniclouddrive.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 檔案管理 API
 * 處理檔案上傳、下載、刪除等操作
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上傳檔案
     * 
     * @param request 檔案上傳請求，包含：
     *                - file: 上傳的檔案（必填）
     *                - folderId: 目標資料夾 ID（選填，null 表示上傳到根目錄）
     *                - duplicateAction: 重複檔案處理方式（選填）
     *                - null: 拒絕上傳，回傳錯誤讓前端顯示選項
     *                - 0: 覆蓋現有檔案
     *                - 1: 自動加後綴（如 file(1).pdf）
     * @return 上傳結果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @ModelAttribute FileUploadRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();
        FileUploadResponse response = fileService.uploadFile(
                request.getFile(),
                request.getFolderId(),
                request.getDuplicateAction(),
                userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
