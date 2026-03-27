package com.toit.view.pagefolders;

import com.toit.folders.FoldersService;
import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.items.attachments.AttachMentsService;
import com.toit.items.links.LinksService;
import com.toit.items.texts.TextsService;
import com.toit.view.pagefolders.dto.response.PageFoldersFoldersItemResponse;
import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageFoldersUseCaseImpl implements PageFoldersUseCase {
    private final FoldersService foldersService;
    private final TextsService textsService;
    private final LinksService linksService;
    private final AttachMentsService attachMentsService;

    @Override
    public PageFoldersMemoResponse getOneFoldersMemo(Long usersId, Long foldersId) {
        return foldersService.getOneFoldersMemobByUser(usersId, foldersId);
    }

    @Override
    public List<PageFoldersFoldersItemResponse> getAllFoldersByUser(Long usersId) {
        List<FoldersItemResponse> folders = foldersService.getAllFoldersByUser(usersId);
        List<PageFoldersFoldersItemResponse> result = new ArrayList<>();
        for (FoldersItemResponse folder : folders) {
            long itemsCount = textsService.countByFoldersId(folder.getFoldersId())
                    + linksService.countByFoldersId(folder.getFoldersId())
                    + attachMentsService.countByFoldersId(folder.getFoldersId());
            result.add(new PageFoldersFoldersItemResponse(folder, itemsCount));
        }
        return result;
    }

    @Override
    public List<FoldersItemResponse> searchFolders(Long usersId, String keyword) {
        return foldersService.searchFolders(usersId, keyword);
    }
}