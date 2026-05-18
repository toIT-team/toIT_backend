package com.toit.items.attachments.dto.response;

import java.util.List;
import lombok.Getter;

@Getter
public class AttachMentsImagesCreateTimedResponse {
    private final List<AttachMentsImagesCreateInFoldersResponse> results;
    private final UploadTimingsDto timings;

    public AttachMentsImagesCreateTimedResponse(
            List<AttachMentsImagesCreateInFoldersResponse> results,
            UploadTimingsDto timings) {
        this.results = results;
        this.timings = timings;
    }
}