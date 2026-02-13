package com.toit.exception;

import com.toit.exception.users.UsersNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Users -> 404, 사용자를 찾을 수 없음
     */
    @ExceptionHandler(UsersNotFoundException.class)
    public ResponseEntity<ErrorResponse> UsersNotFoundException(UsersNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


}
