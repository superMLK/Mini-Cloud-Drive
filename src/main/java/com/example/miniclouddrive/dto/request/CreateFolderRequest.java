package com.example.miniclouddrive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立資料夾請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "建立資料夾請求")
public class CreateFolderRequest {

    @Schema(description = "資料夾名稱", example = "我的文件", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "資料夾名稱不能為空")
    @Size(min = 1, max = 255, message = "資料夾名稱長度需在 1-255 字元之間")
    private String name;

    @Schema(description = "父資料夾 ID（null 表示建立在根目錄）", example = "1", nullable = true)
    private Long parentId;
}
