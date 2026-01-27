package com.example.miniclouddrive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiReturnCode {
    SUCCESS("0000", "成功"),
    INVALID_PARAM("1001", "參數錯誤"),
    UNAUTHORIZED("1002", "未授權"),
    NOT_FOUND("1003", "找不到資源"),
    FILE_ALREADY_EXISTS("1006", "檔案已存在"),
    INSUFFICIENT_STORAGE("1007", "儲存空間不足"),
    FILE_STORAGE_ERROR("1008", "檔案儲存失敗"),
    INVALID_FOLDER("1009", "無效的資料夾"),
    SERVER_ERROR("9999", "伺服器錯誤"),
    JWT_ERROR("2001", "JWT處理錯誤");

    private final String code;
    private final String message;
}
