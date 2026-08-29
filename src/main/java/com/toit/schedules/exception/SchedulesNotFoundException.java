package com.toit.schedules.exception;

// 일정에 관한 404 에러
public class SchedulesNotFoundException extends RuntimeException {
    public SchedulesNotFoundException(String message) {super(message);
    }
}
