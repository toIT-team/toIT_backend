-- 유실 측정용 일정 100건 + 알림 예약 100건을 만든다.
-- 제목이 '유실테스트-' 로 시작하므로 나중에 이것만 골라 지울 수 있다.
--
-- 실행 전에 아래 users_id 를 본인 것으로 바꾼다.

SET @USERS_ID = 32;

-- 1. 일정 100건
--    재귀 CTE 대신 0~9 두 벌을 교차시켜 1~100 을 만든다. MySQL 버전을 안 탄다.
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('유실테스트-', LPAD(nums.n, 3, '0')),
    'blue300',
    NULL,
    true,
    CURDATE(),
    CURDATE(),
    '23:00:00',
    '23:30:00',
    'ACTIVE',
    '',
    NOW(),
    @USERS_ID
FROM (
    SELECT a.d + b.d * 10 + 1 AS n
    FROM (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) a
    CROSS JOIN
         (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) b
) nums;

-- 2. 각 일정에 알림 예약 하나씩
--    시각은 02_reset.sql 에서 다시 맞추므로 여기서는 아무 값이나 둔다.
INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes, is_sent, is_read
)
SELECT s.schedules_id, true, NOW(), 0, false, false
FROM schedules s
WHERE s.users_id = @USERS_ID
  AND s.title LIKE '유실테스트-%';

-- 3. 몇 건 만들어졌는지
SELECT COUNT(*) AS 만든_알림_건수
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';
