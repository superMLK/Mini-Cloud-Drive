package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立資料夾成功回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "建立資料夾成功回應")
public class CreateFolderResponse {

    @Schema(description = "新建資料夾 ID", example = "1")
    private Long folderId;

    @Schema(description = "資料夾名稱", example = "我的文件")
    private String name;

    @Schema(description = "父資料夾 ID（null 表示根目錄）", example = "1", nullable = true)
    private Long parentId;
}
