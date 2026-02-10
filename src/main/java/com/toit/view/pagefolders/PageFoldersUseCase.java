package com.toit.view.pagefolders;

import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;

public interface PageFoldersUseCase {
    PageFoldersMemoResponse getOneFoldersMemo(Long usersId, Long foldersId);
}
