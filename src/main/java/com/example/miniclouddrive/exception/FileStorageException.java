package com.example.miniclouddrive.exception;

/**
 * 檔案儲存失敗例外
 * 當檔案系統 I/O 操作失敗時拋出
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
