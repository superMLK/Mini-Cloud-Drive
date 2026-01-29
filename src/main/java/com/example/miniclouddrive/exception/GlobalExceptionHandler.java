package com.example.miniclouddrive.exception;

import com.example.miniclouddrive.dto.response.ApiResponseCode;
import com.example.miniclouddrive.dto.response.ApiReturnCode;
import com.example.miniclouddrive.dto.response.FileExistsResponse;
import com.example.miniclouddrive.dto.response.StorageQuotaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponseCode<Void>> handleBusinessException(BusinessException ex) {
                return ResponseEntity.badRequest().body(
                                ApiResponseCode.failure(
                                                ex.getRtnCode(),
                                                ex.getRtnMsg()));
        }

        /** Request定義如果不符合Validation規則，會拋出此異常 */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponseCode<Void>> handleValidationException(MethodArgumentNotValidException ex) {
                String msg = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(
                                                ApiResponseCode.failure(
                                                                ApiReturnCode.INVALID_PARAM.getCode(),
                                                                msg));
        }

        /** 檔案已存在例外 */
        @ExceptionHandler(FileAlreadyExistsException.class)
        public ResponseEntity<ApiResponseCode<FileExistsResponse>> handleFileAlreadyExistsException(
                        FileAlreadyExistsException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(
                                                ApiResponseCode.failure(
                                                                ApiReturnCode.FILE_ALREADY_EXISTS.getCode(),
                                                                ApiReturnCode.FILE_ALREADY_EXISTS.getMessage(),
                                                                ex.toResponse()));
        }

        /** 儲存空間不足例外 */
        @ExceptionHandler(InsufficientStorageException.class)
        public ResponseEntity<ApiResponseCode<StorageQuotaResponse>> handleInsufficientStorageException(
                        InsufficientStorageException ex) {
                return ResponseEntity
                                .status(HttpStatus.INSUFFICIENT_STORAGE)
                                .body(
                                                ApiResponseCode.failure(
                                                                ApiReturnCode.INSUFFICIENT_STORAGE.getCode(),
                                                                ApiReturnCode.INSUFFICIENT_STORAGE.getMessage(),
                                                                ex.toResponse()));
        }

        /** 檔案儲存失敗例外 */
        @ExceptionHandler(FileStorageException.class)
        public ResponseEntity<ApiResponseCode<Void>> handleFileStorageException(FileStorageException ex) {
                log.error("檔案儲存失敗: {}", ex.getMessage(), ex);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                                ApiResponseCode.failure(
                                                                ApiReturnCode.FILE_STORAGE_ERROR.getCode(),
                                                                ex.getMessage()));
        }

        /** 無效資料夾例外 */
        @ExceptionHandler(InvalidFolderException.class)
        public ResponseEntity<ApiResponseCode<Void>> handleInvalidFolderException(InvalidFolderException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(
                                                ApiResponseCode.failure(
                                                                ApiReturnCode.INVALID_FOLDER.getCode(),
                                                                ex.getMessage()));
        }

        /** 檔案上傳大小超過限制 */
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiResponseCode<Void>> handleMaxUploadSizeExceededException(
                        MaxUploadSizeExceededException ex) {
                return ResponseEntity
                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body(
                                                ApiResponseCode.failure(
                                                                ApiReturnCode.INVALID_PARAM.getCode(),
                                                                "檔案大小超過限制（最大 50MB）"));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponseCode<Void>> handleException(Exception ex) {
                // 列印完整的錯誤堆疊，方便 debug
                log.error("未預期的伺服器錯誤: {}", ex.getMessage(), ex);
                return ResponseEntity.internalServerError().body(
                                ApiResponseCode.failure(
                                                ApiReturnCode.SERVER_ERROR.getCode(),
                                                ApiReturnCode.SERVER_ERROR.getMessage()));
        }
}
