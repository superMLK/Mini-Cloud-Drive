package com.example.miniclouddrive.controller;

import com.example.miniclouddrive.dto.request.CreateFolderRequest;
import com.example.miniclouddrive.dto.request.DeleteFolderRequest;
import com.example.miniclouddrive.dto.request.FileUploadRequest;
import com.example.miniclouddrive.dto.request.RenameFolderRequest;
import com.example.miniclouddrive.dto.response.ApiResponseCode;
import com.example.miniclouddrive.dto.response.CreateFolderResponse;
import com.example.miniclouddrive.dto.response.FileResponse;
import com.example.miniclouddrive.dto.response.FileUploadResponse;
import com.example.miniclouddrive.service.FileService;
import com.example.miniclouddrive.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 檔案管理 API
 * 處理檔案上傳、下載、資料夾管理等操作
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "檔案管理 API", description = "檔案上傳、下載與資料夾管理操作")
public class FileController {

        private final FileService fileService;

        @Operation(summary = "上傳檔案", description = "上傳檔案到指定資料夾，可設定重複檔案處理方式：\n" +
                        "- duplicateAction = null：拒絕上傳，回傳錯誤讓前端顯示選項\n" +
                        "- duplicateAction = 0：覆蓋現有檔案\n" +
                        "- duplicateAction = 1：自動加後綴（如 file(1).pdf）")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "上傳成功"),
                        @ApiResponse(responseCode = "400", description = "檔案驗證失敗", content = @Content(schema = @Schema(implementation = ApiResponseCode.class))),
                        @ApiResponse(responseCode = "409", description = "檔案已存在且未指定處理方式", content = @Content(schema = @Schema(implementation = ApiResponseCode.class)))
        })
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponseCode<FileUploadResponse>> uploadFile(
                        @Valid @ModelAttribute FileUploadRequest request) {

                Long userId = SecurityUtils.getCurrentUserId();
                FileUploadResponse response = fileService.uploadFile(
                                request.getFile(),
                                request.getFolderId(),
                                request.getDuplicateAction(),
                                userId);

                return ResponseEntity.ok(ApiResponseCode.success(response));
        }

        @Operation(summary = "建立資料夾", description = "在指定的父資料夾下建立新資料夾，parentId 為 null 表示建立在根目錄")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "建立成功"),
                        @ApiResponse(responseCode = "400", description = "請求驗證失敗", content = @Content(schema = @Schema(implementation = ApiResponseCode.class))),
                        @ApiResponse(responseCode = "404", description = "父資料夾不存在", content = @Content(schema = @Schema(implementation = ApiResponseCode.class))),
                        @ApiResponse(responseCode = "409", description = "同名資料夾已存在", content = @Content(schema = @Schema(implementation = ApiResponseCode.class)))
        })
        @PostMapping("/createFolder")
        public ResponseEntity<ApiResponseCode<CreateFolderResponse>> createNewFolder(
                        @Valid @RequestBody CreateFolderRequest request) {

                Long userId = SecurityUtils.getCurrentUserId();
                CreateFolderResponse response = fileService.createFolder(
                                request.getName(),
                                request.getParentId(),
                                userId);

                return ResponseEntity.ok(ApiResponseCode.success(response));
        }

        @Operation(summary = "刪除資料夾", description = "軟刪除資料夾及其所有子項目（遞迴刪除）")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "刪除成功"),
                        @ApiResponse(responseCode = "404", description = "資料夾不存在", content = @Content(schema = @Schema(implementation = ApiResponseCode.class)))
        })
        @PostMapping("/deleteFolder")
        public ResponseEntity<ApiResponseCode<Void>> deleteFolder(
                        @Valid @RequestBody DeleteFolderRequest request) {

                Long userId = SecurityUtils.getCurrentUserId();
                Long id = request.getId();

                fileService.deleteFolder(id, userId);

                return ResponseEntity.ok(ApiResponseCode.success(null));
        }

        @Operation(summary = "重新命名資料夾", description = "修改指定資料夾的名稱，同一層級不可有重複名稱")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "重新命名成功"),
                        @ApiResponse(responseCode = "400", description = "請求驗證失敗", content = @Content(schema = @Schema(implementation = ApiResponseCode.class))),
                        @ApiResponse(responseCode = "404", description = "資料夾不存在", content = @Content(schema = @Schema(implementation = ApiResponseCode.class))),
                        @ApiResponse(responseCode = "409", description = "同名資料夾已存在", content = @Content(schema = @Schema(implementation = ApiResponseCode.class)))
        })
        @PostMapping("/renameFolder")
        public ResponseEntity<ApiResponseCode<Void>> renameFolder(
                        @Valid @RequestBody RenameFolderRequest request) {

                Long userId = SecurityUtils.getCurrentUserId();
                fileService.renameFolder(request.getId(), request.getName(), userId);

                return ResponseEntity.ok(ApiResponseCode.success(null));
        }

        @Operation(summary = "查詢檔案列表", description = "分頁查詢指定資料夾下的檔案與子資料夾")
        @GetMapping("/list")
        public ResponseEntity<ApiResponseCode<Page<FileResponse>>> getFileList(
                        @RequestParam(required = false) Long folderId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Long userId = SecurityUtils.getCurrentUserId();
                Page<FileResponse> fileList = fileService
                                .getFileList(userId, folderId, page, size);

                return ResponseEntity.ok(ApiResponseCode.success(fileList));
        }
}
