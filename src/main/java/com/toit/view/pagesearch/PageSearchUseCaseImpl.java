package com.toit.view.pagesearch;

import com.toit.folders.FoldersService;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.items.attachments.AttachMentsService;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.links.LinksService;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.texts.TextsService;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.schedules.SchedulesService;
import com.toit.schedules.dto.response.ScheduleViewResponse;
import com.toit.user.UsersService;
import com.toit.view.pagesearch.dto.response.PageSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PageSearchUseCaseImpl implements PageSearchUseCase {
    private final FoldersService foldersService;
    private final UsersService usersService;
    private final SchedulesService schedulesService;
    private final TextsService textsService;
    private final LinksService linksService;
    private final AttachMentsService attachMentsService;

    @Override
    @Transactional(readOnly = true)
    public PageSearchResponse search(Long usersId, String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) {
            return PageSearchResponse.empty();
        }

        usersService.findById(usersId);

        List<FoldersItemResponse> folders = foldersService.searchFolders(usersId, keyword);
        List<ScheduleViewResponse> schedules = schedulesService.searchSchedules(usersId, keyword);
        List<LinksGetInFoldersResponse> links = linksService.searchLinks(usersId, keyword);
        List<TextsGetInFoldersResponse> texts = textsService.searchTexts(usersId, keyword);
//        List<AttachMentsImagesGetInFoldersResponse> images = attachMentsService.searchImages(usersId, keyword);
        List<AttachMentsFilesGetInFoldersResponse> files = attachMentsService.searchFiles(usersId, keyword);

        return new PageSearchResponse(folders, schedules, links, texts, files);
    }
}
