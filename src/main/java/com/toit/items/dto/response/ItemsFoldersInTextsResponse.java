package com.toit.items.dto.response;

import com.toit.items.Items;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemsFoldersInTextsResponse {
    private Long itemsId;
    /**
     * 메모 본문
     */
    private String textContent;

    /**
     * 자료 생성시간
     */
    private LocalDateTime createdAt;

    public ItemsFoldersInTextsResponse(Long itemsId, String textContent, LocalDateTime createdAt){
        this.itemsId = itemsId;
        this.textContent = textContent;
        this.createdAt = createdAt;
    }

    public ItemsFoldersInTextsResponse(Items item){
        this.itemsId = item.getItemsId();
        this.textContent = item.getTextContent();
        this.createdAt = item.getCreatedAt();
    }
}
