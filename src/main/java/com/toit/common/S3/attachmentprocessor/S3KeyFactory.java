package com.toit.common.S3.attachmentprocessor;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class S3KeyFactory {

    public String imageKey(Long usersId, String ext) {
        return "users/" + usersId
                + "/images/" + UUID.randomUUID() + "." + ext;
    }

    public String fileKey(Long usersId, String ext) {
        return "users/" + usersId
                + "/files/" + UUID.randomUUID() + "." + ext;
    }
}