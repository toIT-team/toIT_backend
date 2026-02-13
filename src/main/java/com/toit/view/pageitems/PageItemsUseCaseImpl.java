package com.toit.view.pageitems;

import com.toit.items.ItemsService;
import com.toit.items.dto.response.ItemsFoldersImagesResponse;
import com.toit.items.dto.response.ItemsFoldersInFilesResponse;
import com.toit.items.dto.response.ItemsFoldersInLinksResponse;
import com.toit.items.dto.response.ItemsFoldersInTextsResponse;
import com.toit.view.pageitems.dto.response.PageFoldersInItemsAllResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageItemsUseCaseImpl implements PageItemsUseCase{
    private final ItemsService itemsService;

    @Override
    public PageFoldersInItemsAllResponse getOnFoldersInItemsAll(Long usersId, Long foldersId) {
        List<ItemsFoldersInLinksResponse> links = itemsService.getFoldersLinks(usersId, foldersId);
        List<ItemsFoldersInTextsResponse> texts = itemsService.getFoldersTexts(usersId, foldersId);
        List<ItemsFoldersInFilesResponse> files = itemsService.getFoldersFiles(usersId, foldersId);
        List<ItemsFoldersImagesResponse> images = itemsService.getFoldersImages(usersId, foldersId);

        return new PageFoldersInItemsAllResponse(usersId, foldersId, links, texts, files, images);
    }
}
