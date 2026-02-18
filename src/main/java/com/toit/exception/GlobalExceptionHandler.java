package com.toit.exception;

import com.toit.exception.items.attachments.AttachmentReadException;
import com.toit.exception.items.attachments.UnsupportedImageTypeException;
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

    /***
     *Schedules -> 404, 일정을 찾을 수 없음
     */
    @ExceptionHandler(SchedulesNotFoundException.class)
    public ResponseEntity<ErrorResponse> SchedulesNotFoundException(SchedulesNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * items의 Image 지정된 파일이 아닐 경우 -> 400
     */
    @ExceptionHandler(UnsupportedImageTypeException.class)
    public ResponseEntity<ErrorResponse> UnsupportedImageTypeException(UnsupportedImageTypeException ex) {
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    /**
     * items의 Image 지정된 파일이 아닐 경우 -> 400
     */
    @ExceptionHandler(AttachmentReadException.class)
    public ResponseEntity<ErrorResponse> AttachmentReadException(AttachmentReadException ex) {
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


}
