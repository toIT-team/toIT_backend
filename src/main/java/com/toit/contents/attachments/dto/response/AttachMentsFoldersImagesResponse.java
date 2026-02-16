package com.toit.contents.attachments.dto.response;

import com.toit.contents.attachments.AttachMents;
import com.toit.contents.attachments.Contents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachMentsFoldersImagesResponse {
    private Long attachmentsId;

    /**
     * 이미지 URL
     */
    private String filePath;

    /**
     * 이미지 메모
     */
    private String textContent;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public AttachMentsFoldersImagesResponse(Long attachmentsId, String filePath, String textContent, LocalDateTime createdAt){
        this.attachmentsId = attachmentsId;
        this.filePath = filePath;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }

    public AttachMentsFoldersImagesResponse(AttachMents attachments){
        this.attachmentsId = attachments.getAttachmentsId();
        this.filePath = attachments.getFilePath();
        this.textContent = attachments.getTextContent();
        this.createdAt = attachments.getCreatedAt();
    }
}
