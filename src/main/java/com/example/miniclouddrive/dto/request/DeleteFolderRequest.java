package com.example.miniclouddrive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刪除資料夾請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刪除資料夾請求")
public class DeleteFolderRequest {

    @Schema(description = "資料夾 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "資料夾 ID 不能為空")
    private Long id;
}
