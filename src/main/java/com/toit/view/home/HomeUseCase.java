package com.toit.view.home;

import com.toit.view.home.dto.response.HomeViewResponse;
import java.time.LocalDate;

public interface HomeUseCase {

    HomeViewResponse getHomeView(Long usersId, LocalDate todayDate);
}