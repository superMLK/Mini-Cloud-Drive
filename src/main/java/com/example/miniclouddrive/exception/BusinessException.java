package com.example.miniclouddrive.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final String rtnCode;
    private final String rtnMsg;
}
