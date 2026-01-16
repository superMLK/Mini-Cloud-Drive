package com.example.miniclouddrive.exception;

import com.example.miniclouddrive.dto.response.ApiResponse;
import com.example.miniclouddrive.dto.response.ApiReturnCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        BusinessException be = new BusinessException(ApiReturnCode.INVALID_PARAM.getCode(), msg);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.failure(
                                be.getRtnCode(),
                                be.getRtnMsg()
                        )
                );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.failure(
                        ex.getRtnCode(),
                        ex.getRtnMsg()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.failure(
                        ApiReturnCode.SERVER_ERROR.getCode(),
                        ApiReturnCode.SERVER_ERROR.getMessage()
                )
        );
    }
}
