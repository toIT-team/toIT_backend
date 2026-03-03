package com.toit.view.pagesearch;

import com.toit.view.pagesearch.dto.response.PageSearchResponse;

public interface PageSearchUseCase {
    public PageSearchResponse search(Long usersId, String keyword);
}
