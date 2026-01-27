package com.example.miniclouddrive.exception;

import com.example.miniclouddrive.dto.response.FileExistsResponse;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 檔案已存在例外
 * 當使用者嘗試上傳重複檔名且未指定處理方式時拋出
 */
@Getter
public class FileAlreadyExistsException extends RuntimeException {
    private final Long existingFileId;
    private final String existingFileName;
    private final LocalDateTime uploadTime;

    public FileAlreadyExistsException(Long existingFileId, String existingFileName, LocalDateTime uploadTime) {
        super("檔案已存在: " + existingFileName);
        this.existingFileId = existingFileId;
        this.existingFileName = existingFileName;
        this.uploadTime = uploadTime;
    }

    /**
     * 轉換為回應 DTO
     */
    public FileExistsResponse toResponse() {
        return FileExistsResponse.builder()
                .existingFileId(existingFileId)
                .existingFileName(existingFileName)
                .uploadTime(uploadTime)
                .build();
    }
}
