package com.toit.view.folders;

import com.toit.view.folders.dto.response.PageFoldersMemoResponse;

public interface PageFoldersUseCase {
    PageFoldersMemoResponse getOneFoldersMemo(Long usersId, Long foldersId);
}
