package com.toit.exception.items.attachments;

public class AttachmentsNotFoundException extends RuntimeException {

    public AttachmentsNotFoundException(String message) {
        super(message);
    }
}