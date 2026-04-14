package com.toit.view.pagemy;

import com.toit.view.pagemy.dto.response.PageMyViewResponse;

public interface PageMyUseCase {

    PageMyViewResponse getMyView(Long usersId);
}
