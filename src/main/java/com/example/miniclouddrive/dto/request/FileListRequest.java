package com.example.miniclouddrive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "檔案列表查詢請求")
public class FileListRequest {

    @Schema(description = "資料夾 ID (選填，若為空則查詢根目錄或所有檔案)")
    private Long folderId;

    @Schema(description = "分頁頁碼 (預設 0)")
    private Integer page = 0;

    @Schema(description = "每頁筆數 (預設 10)")
    private Integer size = 10;
}
