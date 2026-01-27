package com.example.miniclouddrive.exception;

/**
 * 無效資料夾例外
 * 當指定的 folderId 不存在或不屬於該使用者時拋出
 */
public class InvalidFolderException extends RuntimeException {

    public InvalidFolderException(String message) {
        super(message);
    }

    public InvalidFolderException(Long folderId) {
        super("無效的資料夾 ID: " + folderId);
    }
}
