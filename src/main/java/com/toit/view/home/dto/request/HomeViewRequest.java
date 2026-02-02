package com.toit.view.home.dto.request;

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

    public HomeViewRequest(Long usersId){
        this.usersId = usersId;
    }
}
