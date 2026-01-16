package com.example.miniclouddrive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String rtnCode;
    private String rtnMsg;
    private T data;

    /** 成功的回應，不帶資料 */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ApiReturnCode.SUCCESS.getCode(), ApiReturnCode.SUCCESS.getMessage(), null);
    }

    /** 成功的回應，帶有資料 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiReturnCode.SUCCESS.getCode(), ApiReturnCode.SUCCESS.getMessage(), data);
    }

    /** 成功的回應，帶有自訂訊息和資料 */
    public static <T> ApiResponse<T> success(String rtnMsg, T data) {
        return new ApiResponse<>(ApiReturnCode.SUCCESS.getCode(), rtnMsg, data);
    }

    /** 失敗的回應，帶有錯誤訊息 */
    public static <T> ApiResponse<T> failure(String rtnCode, String rtnMsg) {
        return new ApiResponse<>(rtnCode, rtnMsg, null);
    }

    /** 失敗的回應，帶有錯誤訊息和資料 */
    public static <T> ApiResponse<T> failure(String rtnCode, String rtnMsg, T data) {
        return new ApiResponse<>(rtnCode, rtnMsg, data);
    }
}
