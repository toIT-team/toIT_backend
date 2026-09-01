package com.toit.view.pagenotifications.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 알림 화면이 탭 셋으로 나뉘어 있어 종류별로 갈라서 내려준다.
 *
 * 한 번 부르면 세 탭이 다 채워지므로 탭을 옮길 때 다시 부르지 않아도 된다.
 * 각 묶음은 보낸 시각 최신순이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageNotificationsViewResponse {

    /** 공지사항 */
    private List<NotificationViewResponse> notices;

    /** 문의 답변 */
    private List<NotificationViewResponse> feedbacks;

    /** 일정 알림 */
    private List<NotificationViewResponse> schedules;

    public PageNotificationsViewResponse(List<NotificationViewResponse> notices,
                                         List<NotificationViewResponse> feedbacks,
                                         List<NotificationViewResponse> schedules) {
        this.notices = notices;
        this.feedbacks = feedbacks;
        this.schedules = schedules;
    }
}
