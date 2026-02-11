package com.toit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private int code;
    private String message;

    public ErrorResponse(int code, String message){
        this.code = code;
        this.message = message;
    }
}
