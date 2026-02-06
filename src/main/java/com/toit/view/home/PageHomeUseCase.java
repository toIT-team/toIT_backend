package com.toit.view.home;

import com.toit.view.home.dto.response.PageHomeViewResponse;
import java.time.LocalDate;

public interface PageHomeUseCase {

    PageHomeViewResponse getHomeView(Long usersId, LocalDate todayDate);
}