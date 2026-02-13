package com.toit.view.pageitems.dto.response;

import com.toit.items.dto.response.ItemsFoldersImagesResponse;
import com.toit.items.dto.response.ItemsFoldersInFilesResponse;
import com.toit.items.dto.response.ItemsFoldersInLinksResponse;
import com.toit.items.dto.response.ItemsFoldersInTextsResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageFoldersInItemsAllResponse {
    private Long usersId;

    private Long foldersId;
    private List<ItemsFoldersInLinksResponse> links;
    private List<ItemsFoldersInTextsResponse> texts;

    private List<ItemsFoldersInFilesResponse> files;

    private List<ItemsFoldersImagesResponse> images;

    public PageFoldersInItemsAllResponse(Long usersId, Long foldersId, List<ItemsFoldersInLinksResponse> links, List<ItemsFoldersInTextsResponse> texts,
                                         List<ItemsFoldersInFilesResponse> files, List<ItemsFoldersImagesResponse> images){
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.links = links;
        this.texts = texts;
        this.files = files;
        this.images = images;
    }

}
