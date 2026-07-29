package com.toit.exception;

import com.toit.exception.auth.InvalidTokenException;
import com.toit.exception.folders.FoldersNotFoundException;
import com.toit.exception.items.attachments.AttachmentFileNameUpdateNotAllowedException;
import com.toit.exception.items.attachments.AttachmentReadException;
import com.toit.exception.items.attachments.AttachmentsNotFoundException;
import com.toit.exception.items.attachments.UnsupportedFileTypeException;
import com.toit.exception.items.attachments.UnsupportedImageTypeException;
import com.toit.exception.items.links.LinksNotFoundException;
import com.toit.exception.items.texts.TextsNotFoundException;
import com.toit.exception.schedules.ScheduleTimeRangeException;
import com.toit.exception.schedules.SchedulesNotFoundException;
import com.toit.exception.schedulesalarm.SchedulesAlarmNotFoundException;
import com.toit.exception.users.UsersNotFoundException;
import com.toit.exception.userssettings.UsersSettingsNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * <h1>Auth</h1>
     */

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> InvalidTokenException(InvalidTokenException ex) {
        log.warn("[401] InvalidTokenException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * <h1>Users</h1>
     */

    /**
     * Users -> 404, 사용자를 찾을 수 없음
     */
    @ExceptionHandler(UsersNotFoundException.class)
    public ResponseEntity<ErrorResponse> UsersNotFoundException(UsersNotFoundException ex) {
        log.warn("[404] UsersNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * UsersSettings -> 404, 사용자 설정 정보를 찾을 수 없음
     */
    @ExceptionHandler(UsersSettingsNotFoundException.class)
    public ResponseEntity<ErrorResponse> UsersSettingsNotFoundException(UsersSettingsNotFoundException ex) {
        log.warn("[404] UsersSettingsNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * <h1>folders</h1>
     */
    @ExceptionHandler(FoldersNotFoundException.class)
    public ResponseEntity<ErrorResponse> FoldersNotFoundException(UsersNotFoundException ex) {
        log.warn("[404] FoldersNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    /**
     * <h1>Schedules</h1>
     */

    /***
     *Schedules -> 404, 일정을 찾을 수 없음
     */
    @ExceptionHandler(SchedulesNotFoundException.class)
    public ResponseEntity<ErrorResponse> SchedulesNotFoundException(SchedulesNotFoundException ex) {
        log.warn("[404] SchedulesNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 일정 생성/수정 시 종료 시간이 시작 시간보다 빠른 경우 400 응답을 반환한다.
     */
    @ExceptionHandler(ScheduleTimeRangeException.class)
    public ResponseEntity<ErrorResponse> ScheduleTimeRangeException(ScheduleTimeRangeException ex) {
        log.warn("[400] ScheduleTimeRangeException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SchedulesAlarmNotFoundException.class)
    public ResponseEntity<ErrorResponse> SchedulesAlarmNotFoundException(SchedulesAlarmNotFoundException ex) {
        log.warn("[404] SchedulesAlarmNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * <h1>items</h1>
     */

    /**
     * items의 Image 지정된 파일이 아닐 경우 -> 400
     */
    @ExceptionHandler(UnsupportedImageTypeException.class)
    public ResponseEntity<ErrorResponse> UnsupportedImageTypeException(UnsupportedImageTypeException ex) {
        log.warn("[400] UnsupportedImageTypeException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * items의 file 지정된 파일이 아닐 경우 -> 400
     */
    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponse> UnsupportedFileTypeException(UnsupportedFileTypeException ex) {
        log.warn("[400] UnsupportedFileTypeException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AttachmentFileNameUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> AttachmentFileNameUpdateNotAllowedException(AttachmentFileNameUpdateNotAllowedException ex) {
        log.warn("[400] AttachmentFileNameUpdateNotAllowedException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * items의 서버에서 이미지 및 파일을 읽지 못한 경우 -> 500
     */
    @ExceptionHandler(AttachmentReadException.class)
    public ResponseEntity<ErrorResponse> AttachmentReadException(AttachmentReadException ex) {
        log.error("[500] AttachmentReadException: {}", ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * items의 서버에서 텍스트가 없는 경우 -> 404
     */
    @ExceptionHandler(TextsNotFoundException.class)
    public ResponseEntity<ErrorResponse> TextsNotFoundException(TextsNotFoundException ex) {
        log.warn("[404] TextsNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * items의 서버에서 링크가 없는 경우 -> 404
     */
    @ExceptionHandler(LinksNotFoundException.class)
    public ResponseEntity<ErrorResponse> LinksNotFoundException(LinksNotFoundException ex) {
        log.warn("[404] LinksNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * items에서 이미지/파일이 없는 경우 -> 404
     */
    @ExceptionHandler(AttachmentsNotFoundException.class)
    public ResponseEntity<ErrorResponse> AttachmentsNotFoundException(AttachmentsNotFoundException ex) {
        log.warn("[404] AttachmentsNotFoundException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 비즈니스 규칙 위반 (보관함 20개 초과, 스토리지 용량 초과 등) -> 400
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.warn("[400] IllegalStateException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 잘못된 인자 (즐겨찾기 상태 동일 등) -> 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("[400] IllegalArgumentException: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @Valid 검증 실패 (닉네임 길이 초과 등) -> 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");
        log.warn("[400] MethodArgumentNotValidException: {}", message);
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 처리되지 않은 예외 -> 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("서버 내부 오류 발생", ex);
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
