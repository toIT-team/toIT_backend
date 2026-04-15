package com.toit.view.pageitems.dto.response;

import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageFoldersInItemsAllResponse {

    private Long foldersId;
    private List<LinksGetInFoldersResponse> links;
    private List<TextsGetInFoldersResponse> texts;
    private List<AttachMentsFilesGetInFoldersResponse> files;
    private List<AttachMentsImagesGetInFoldersResponse> images;

    public PageFoldersInItemsAllResponse(Long foldersId, List<LinksGetInFoldersResponse> links,
                                         List<TextsGetInFoldersResponse> texts,
                                         List<AttachMentsFilesGetInFoldersResponse> files,
                                         List<AttachMentsImagesGetInFoldersResponse> images) {
        this.foldersId = foldersId;
        this.links = links;
        this.texts = texts;
        this.files = files;
        this.images = images;
    }
}