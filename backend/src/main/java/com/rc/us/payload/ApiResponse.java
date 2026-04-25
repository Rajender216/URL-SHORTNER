package com.rc.us.payload;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private Integer statusCode;
    private T data;
}
