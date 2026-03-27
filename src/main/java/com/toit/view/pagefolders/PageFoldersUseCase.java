package com.toit.view.pagefolders;

import com.toit.folders.dto.response.FoldersItemResponse;
import com.toit.view.pagefolders.dto.response.PageFoldersFoldersItemResponse;
import com.toit.view.pagefolders.dto.response.PageFoldersMemoResponse;
import java.util.List;

public interface PageFoldersUseCase {
    PageFoldersMemoResponse getOneFoldersMemo(Long usersId, Long foldersId);
    List<PageFoldersFoldersItemResponse> getAllFoldersByUser(Long usersId);
    List<FoldersItemResponse> searchFolders(Long usersId, String keyword);
}
