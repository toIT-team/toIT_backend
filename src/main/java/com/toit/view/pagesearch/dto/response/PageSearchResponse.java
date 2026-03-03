package com.toit.view.pagesearch.dto.response;

import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.schedules.dto.response.ScheduleViewResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageSearchResponse {

    List<FoldersItemResponse> folders;
    List<ScheduleViewResponse> schedules;
    List<LinksGetInFoldersResponse> links;
    List<TextsGetInFoldersResponse> texts;
    List<AttachMentsImagesGetInFoldersResponse> images;
    List<AttachMentsFilesGetInFoldersResponse> files;

    public PageSearchResponse(List<FoldersItemResponse> folders, List<ScheduleViewResponse> schedules,
                              List<LinksGetInFoldersResponse> links, List<TextsGetInFoldersResponse> texts,
                              List<AttachMentsImagesGetInFoldersResponse> images, List<AttachMentsFilesGetInFoldersResponse> files)
    {
        this.folders = folders;
        this.schedules = schedules;
        this.links = links;
        this.texts = texts;
        this.images = images;
        this.files = files;
    }

    public static PageSearchResponse empty() {
        return new PageSearchResponse(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

}
