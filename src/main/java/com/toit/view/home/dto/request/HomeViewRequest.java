package com.toit.view.home.dto.request;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeViewRequest {

    /**
     * 사용자 ID
     * */
    private Long usersId;

    private LocalDate todayDate;

    public HomeViewRequest(Long usersId, LocalDate todayDate){
        this.usersId = usersId;
        this.todayDate = todayDate;
    }
}
