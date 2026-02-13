package com.toit.items.dto.response;

import com.toit.items.Items;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemsFoldersImagesResponse {
    private Long itemsId;

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

    public ItemsFoldersImagesResponse(Long itemsId, String filePath, String textContent, LocalDateTime createdAt){
        this.itemsId = itemsId;
        this.filePath = filePath;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }

    public ItemsFoldersImagesResponse(Items item){
        this.itemsId = item.getItemsId();
        this.filePath = item.getFilePath();
        this.textContent = item.getTextContent();
        this.createdAt = item.getCreatedAt();
    }
}
