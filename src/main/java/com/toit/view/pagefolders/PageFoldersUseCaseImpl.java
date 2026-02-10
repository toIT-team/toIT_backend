package com.toit.view.pagefolders;

import com.toit.folders.FoldersService;
import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageFoldersUseCaseImpl implements PageFoldersUseCase{
    private final FoldersService foldersService;
    @Override
    public PageFoldersMemoResponse getOneFoldersMemo(Long usersId, Long foldersId) {
        return foldersService.getOneFoldersMemobByUser(usersId, foldersId);
    }
}
