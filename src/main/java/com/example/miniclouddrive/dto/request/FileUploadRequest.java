package com.example.miniclouddrive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "檔案上傳請求")
public class FileUploadRequest {

    @Schema(description = "上傳的檔案", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(description = "目標資料夾 ID（null 表示上傳到根目錄）", example = "1", nullable = true)
    private Long folderId;

    @Schema(description = "重複檔案處理方式：null=拒絕上傳, 0=覆蓋, 1=自動加後綴", example = "1", nullable = true, allowableValues = { "0",
            "1" })
    private Integer duplicateAction;
}
