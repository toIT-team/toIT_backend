package com.toit.schedules;

import com.toit.schedules.dto.request.SchedulesUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SchedulesServiceTest {

    @InjectMocks
    private SchedulesService schedulesService;

    @Mock
    private SchedulesRepository schedulesRepository;

    @Test
    @DisplayName("일정 수정 시 알림(notification)이 false면 alarmDateTime은 null로 저장되어야 한다")
    void 일정수정시_알림을끄면_알림시간은_null이된다() {
        // ==========================================
        // [Given] 1. 기존 일정 (알림 켜짐, 시간 있음)
        // ==========================================
        Schedules existingSchedule = new Schedules(
                "기존 일정",
                "RED",
                null,
                false,
                LocalDate.now(),
                LocalDate.now(),
                null,
                null,
                true, // notification (핵심: 기존에는 알림이 켜져 있었음!)
                "기존 메모",
                null,
                LocalDateTime.of(2026, 2, 22, 13, 50) // alarmDateTime (기존 알림 시간)
        );

        // findBySchedules(1L)이 호출되면 가짜 기존 일정을 반환하도록 설정
        when(schedulesRepository.findById(anyLong())).thenReturn(java.util.Optional.of(existingSchedule));

        // [Given] 2. 클라이언트의 변경한 수정 요청
        // 알림은 끄겠다(false)고 하면서, 시간 데이터는 그대로 보냄
        SchedulesUpdateRequest request = new SchedulesUpdateRequest(
                null,
                1L,
                "수정된 일정",
                "RED",
                null,
                false,
                LocalDate.now(),
                LocalDate.now(),
                null,
                null,
                false,                  // notification  (핵심: 알림 끄기)
                "테스트 메모",
                LocalDateTime.of(2026, 2, 22, 13, 50) // alarmDateTime (시간은 그대로 보내게 됨)
        );

        // [When] 실제 서비스의 업데이트 로직 실행
        schedulesService.updateSchedules(request);

        // [Then] 결과 검증
        assertFalse(existingSchedule.getNotification(), "알림 플래그는 false여야 합니다.");

        // 테스트가 실패
        assertNull(existingSchedule.getAlarmDateTime(), "알림을 껐으므로 alarmDateTime은 반드시 null이어야 합니다.");
    }

}