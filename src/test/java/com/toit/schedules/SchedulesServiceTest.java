package com.toit.schedules;

import com.toit.common.enums.EntityStatus;
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



}