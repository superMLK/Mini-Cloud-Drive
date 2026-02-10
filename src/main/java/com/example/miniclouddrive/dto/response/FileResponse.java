package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "檔案資訊回應")
public class FileResponse {

    @Schema(description = "檔案 ID")
    private Long fileId;

    @Schema(description = "檔案名稱")
    private String fileName;

    @Schema(description = "檔案大小 (Bytes)")
    private Long size;

    @Schema(description = "上傳時間")
    private LocalDateTime uploadTime;
}
