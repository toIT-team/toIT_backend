package com.toit.view.home.dto.request;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageHomeViewRequest {


    private Long usersId;

    private LocalDate todayDate;

    public PageHomeViewRequest(Long usersId, LocalDate todayDate){
        this.usersId = usersId;
        this.todayDate = todayDate;
    }
}
