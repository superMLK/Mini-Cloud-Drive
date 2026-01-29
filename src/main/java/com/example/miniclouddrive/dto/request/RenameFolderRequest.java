package com.example.miniclouddrive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重新命名資料夾請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重新命名資料夾請求")
public class RenameFolderRequest {

    @Schema(description = "資料夾 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "資料夾 ID 不能為空")
    private Long id;

    @Schema(description = "新資料夾名稱", example = "新名稱", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "資料夾名稱不能為空")
    @Size(min = 1, max = 255, message = "資料夾名稱長度需在 1-255 字元之間")
    private String name;
}
