package com.toit.user.exception;

public class UsersNotFoundException extends RuntimeException {

    public UsersNotFoundException(String message) {
        super(message);
    }

}
