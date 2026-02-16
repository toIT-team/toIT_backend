package com.toit.contents.attachments.dto.response;

import com.toit.contents.attachments.Contents;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemsFoldersInFilesResponse {
    private Long itemsId;

    /**
     * 파일 이름
     */
    private String name;

    /**
     * 파일 URL
     */
    private String filePath;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public ItemsFoldersInFilesResponse(Long itemsId, String filePath, LocalDateTime createdAt){
        this.itemsId = itemsId;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    public ItemsFoldersInFilesResponse(Contents item){
        this.itemsId = item.getItemsId();
        this.filePath = item.getFilePath();
        this.createdAt = item.getCreatedAt();
    }
}
