package com.toit.view.pageitems.dto.response;

import com.toit.contents.attachments.dto.response.AttachMentsFoldersImagesResponse;
import com.toit.contents.attachments.dto.response.ItemsFoldersInFilesResponse;
import com.toit.contents.links.dto.response.LinksGetInFoldersResponse;
import com.toit.contents.texts.dto.response.TextsGetInFoldersResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageFoldersInItemsAllResponse {
    private Long usersId;

    private Long foldersId;
    private List<LinksGetInFoldersResponse> links;
    private List<TextsGetInFoldersResponse> texts;

    private List<ItemsFoldersInFilesResponse> files;

    private List<AttachMentsFoldersImagesResponse> images;

    public PageFoldersInItemsAllResponse(Long usersId, Long foldersId, List<LinksGetInFoldersResponse> links , List<TextsGetInFoldersResponse> texts,
                                         List<ItemsFoldersInFilesResponse> files, List<AttachMentsFoldersImagesResponse> images){
        this.usersId = usersId;
        this.foldersId = foldersId;
        this.links = links;
        this.texts = texts;
        this.files = files;
        this.images = images;
    }

}
