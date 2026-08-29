-- 측정용 일정과 알림 예약을 @COUNT 건 만든다.
-- 제목이 '유실테스트-' 로 시작하므로 나중에 이것만 골라 지울 수 있다.
--
-- 실행 전에 users_id 를 본인 것으로 바꾼다.
-- 한계를 찾을 때는 @COUNT 를 100 → 300 → 500 → 1000 으로 올려가며 돌린다.
-- 건수를 바꿀 때는 04_teardown.sql 로 먼저 지우고 다시 만든다.

SET @USERS_ID = 32;
SET @COUNT    = 100;

-- 1. 일정 @COUNT 건
--    재귀 CTE 대신 0~9 세 벌을 교차시켜 1~1000 을 만들고 @COUNT 까지만 쓴다.
--    MySQL 버전을 안 탄다.
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('유실테스트-', LPAD(nums.n, 4, '0')),
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
    SELECT a.d + b.d * 10 + c.d * 100 + 1 AS n
    FROM (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) a
    CROSS JOIN
         (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) b
    CROSS JOIN
         (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) c
) nums
WHERE nums.n <= @COUNT;

-- 2. 각 일정에 알림 예약 하나씩
--    시각은 02_reset.sql 에서 다시 맞추므로 여기서는 아무 값이나 둔다.
INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT s.schedules_id, true, NOW(), 0, 'PENDING', 0, false
FROM schedules s
WHERE s.users_id = @USERS_ID
  AND s.title LIKE '유실테스트-%';

-- 3. 몇 건 만들어졌는지
SELECT COUNT(*) AS 만든_알림_건수
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';
