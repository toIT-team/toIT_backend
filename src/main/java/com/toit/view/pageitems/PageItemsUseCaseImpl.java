package com.toit.view.pageitems;

import com.toit.folders.FoldersService;
import com.toit.items.attachments.AttachMentsService;
import com.toit.items.attachments.dto.response.AttachMentsImagesGetInFoldersResponse;
import com.toit.items.attachments.dto.response.AttachMentsFilesGetInFoldersResponse;
import com.toit.items.links.LinksService;
import com.toit.items.links.dto.response.LinksGetInFoldersResponse;
import com.toit.items.texts.TextsService;
import com.toit.items.texts.dto.response.TextsGetInFoldersResponse;
import com.toit.view.pageitems.dto.response.PageFoldersInItemsAllResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageItemsUseCaseImpl implements PageItemsUseCase{
    private final AttachMentsService attachMentsService;
    private final LinksService linksService;
    private final TextsService textsService;
    private final FoldersService foldersService;

    @Override
    public PageFoldersInItemsAllResponse getOnFoldersInItemsAll(Long usersId, Long foldersId) {
        // 보관함 진입 시 lastViewedAt 업데이트
        foldersService.updateLastViewedAt(usersId, foldersId);

        List<LinksGetInFoldersResponse> links = linksService.getLinksInFolders(usersId, foldersId);
        List<TextsGetInFoldersResponse> texts = textsService.getTextsInFolders(usersId, foldersId);
        List<AttachMentsFilesGetInFoldersResponse> files = attachMentsService.getFilesInFolders(usersId, foldersId);
        List<AttachMentsImagesGetInFoldersResponse> images = attachMentsService.getImagesInFolders(usersId, foldersId);

        return new PageFoldersInItemsAllResponse(foldersId, links, texts, files, images);
    }
}
