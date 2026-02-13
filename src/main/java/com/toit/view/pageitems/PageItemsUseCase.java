package com.toit.view.pageitems;

import com.toit.view.pageitems.dto.response.PageFoldersInItemsAllResponse;

public interface PageItemsUseCase {
    PageFoldersInItemsAllResponse getOnFoldersInItemsAll(Long usersId, Long foldersId);
}
