package com.toit.view.home;

import com.toit.view.home.dto.request.HomeViewRequest;
import com.toit.view.home.dto.response.HomeViewResponse;

public interface HomeUseCase {

    HomeViewResponse getHomeView(Long usersId);
}