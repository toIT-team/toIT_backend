package com.toit.exception.userssettings;

public class UsersSettingsNotFoundException extends RuntimeException {

    public UsersSettingsNotFoundException(String message) {
        super(message);
    }
}