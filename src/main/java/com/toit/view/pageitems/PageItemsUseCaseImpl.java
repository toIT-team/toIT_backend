package com.toit.view.pageitems;

import com.toit.contents.attachments.AttachMentsService;
import com.toit.contents.attachments.dto.response.AttachMentsFoldersImagesResponse;
import com.toit.contents.attachments.dto.response.ItemsFoldersInFilesResponse;
import com.toit.contents.links.LinksService;
import com.toit.contents.links.dto.response.LinksGetInFoldersResponse;
import com.toit.contents.texts.TextsService;
import com.toit.contents.texts.dto.response.TextsGetInFoldersResponse;
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

    @Override
    public PageFoldersInItemsAllResponse getOnFoldersInItemsAll(Long usersId, Long foldersId) {
        List<LinksGetInFoldersResponse> links = linksService.getLinksInFolders(usersId, foldersId);
        List<TextsGetInFoldersResponse> texts = textsService.getTextsInFolders(usersId, foldersId);
        List<ItemsFoldersInFilesResponse> files = attachMentsService.getFoldersFiles(usersId, foldersId);
        List<AttachMentsFoldersImagesResponse> images = attachMentsService.getFoldersImages(usersId, foldersId);

        return new PageFoldersInItemsAllResponse(usersId, foldersId, links, texts, files, images);
    }
}
