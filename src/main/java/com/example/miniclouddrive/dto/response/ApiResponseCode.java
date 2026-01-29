package com.example.miniclouddrive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一 API 回應格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "統一 API 回應格式")
public class ApiResponseCode<T> {

    @Schema(description = "回應代碼", example = "0000")
    private String rtnCode;

    @Schema(description = "回應訊息", example = "操作成功")
    private String rtnMsg;

    @Schema(description = "回應資料")
    private T data;

    /** 成功的回應，不帶資料 */
    public static <T> ApiResponseCode<T> success() {
        return new ApiResponseCode<>(ApiReturnCode.SUCCESS.getCode(), ApiReturnCode.SUCCESS.getMessage(), null);
    }

    /** 成功的回應，帶有資料 */
    public static <T> ApiResponseCode<T> success(T data) {
        return new ApiResponseCode<>(ApiReturnCode.SUCCESS.getCode(), ApiReturnCode.SUCCESS.getMessage(), data);
    }

    /** 成功的回應，帶有自訂訊息和資料 */
    public static <T> ApiResponseCode<T> success(String rtnMsg, T data) {
        return new ApiResponseCode<>(ApiReturnCode.SUCCESS.getCode(), rtnMsg, data);
    }

    /** 失敗的回應，帶有錯誤訊息 */
    public static <T> ApiResponseCode<T> failure(String rtnCode, String rtnMsg) {
        return new ApiResponseCode<>(rtnCode, rtnMsg, null);
    }

    /** 失敗的回應，帶有錯誤訊息和資料 */
    public static <T> ApiResponseCode<T> failure(String rtnCode, String rtnMsg, T data) {
        return new ApiResponseCode<>(rtnCode, rtnMsg, data);
    }
}
