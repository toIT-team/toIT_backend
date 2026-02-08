package com.toit.view.pagehome;

import com.toit.view.pagehome.dto.response.PageHomeViewResponse;
import java.time.LocalDate;

public interface PageHomeUseCase {

    PageHomeViewResponse getHomeView(Long usersId, LocalDate todayDate);
}