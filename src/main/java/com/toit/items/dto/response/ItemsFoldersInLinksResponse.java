package com.toit.items.dto.response;

import com.toit.items.Items;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemsFoldersInLinksResponse {
    private Long itemsId;
    /**
     * 링크 제목
     */
    private String name;

    /**
     * 링크 URL
     */
    private String filePath;

    /**
     * 링크 본문
     */
    private String textContent;

    /**
     * 링크 썸네일
     */
    private String linkThumbnail;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public ItemsFoldersInLinksResponse(Long itemsId, String name, String filePath, String textContent, String linkThumbnail, LocalDateTime createdAt){
        this.itemsId = itemsId;
        this.name = name;
        this.filePath = filePath;
        this.textContent = textContent;
        this.linkThumbnail = linkThumbnail;
        this.createdAt = createdAt;
    }

    public ItemsFoldersInLinksResponse(Items item){
        this.itemsId = item.getItemsId();
        this.name = item.getName();
        this.filePath = item.getFilePath();
        this.textContent = item.getTextContent();
        this.linkThumbnail = item.getLinkThumbnail();
        this.createdAt = item.getCreatedAt();
    }
}
