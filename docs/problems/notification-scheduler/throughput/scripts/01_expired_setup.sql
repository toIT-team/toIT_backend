-- 만료된 알림이 정말 조용히 사라지는지 확인한다.
--
-- 밀리는 상황을 만들 필요가 없다. 3.2 가 말하는 것은 "시한을 넘긴 뒤에 무슨 일이
-- 나는가" 이므로, 처음부터 넘긴 채로 만들어두면 된다.
--
-- 이 스크립트로 만든 알림은 조회에서 아예 안 걸리므로 **푸시가 한 건도 안 나간다.**
-- 폰이 조용하고, VALID_MINUTES 를 고쳐 배포할 필요도 없다.
--
-- 실행 전에 @USERS_ID 를 본인 것으로 바꾼다.

SET @USERS_ID = 32;
SET @COUNT    = 200;
SET @AGE_MIN  = 11;     -- 시한(10분)보다 1분 더 지난 상태로 만든다


-- ─────────────────────────────────────────────────────────────
-- [1] 앞 라운드 정리
-- ─────────────────────────────────────────────────────────────
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '만료테스트-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '만료테스트-%';

DELETE FROM schedules WHERE title LIKE '만료테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [2] 일정 @COUNT 건
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('만료테스트-', LPAD(nums.n, 4, '0')),
    'blue300', NULL, true,
    CURDATE(), CURDATE(), '23:00:00', '23:30:00',
    'ACTIVE', '', NOW(), @USERS_ID
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


-- ─────────────────────────────────────────────────────────────
-- [3] 알림 예약 — 이미 시한을 넘긴 시각으로
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT
    s.schedules_id,
    true,
    DATE_SUB(NOW(), INTERVAL @AGE_MIN MINUTE),   -- 11분 전
    5,
    'PENDING',
    0,
    false
FROM schedules s
WHERE s.users_id = @USERS_ID
  AND s.title LIKE '만료테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [4] 대조 — 시한 조건 빼면 걸리는가
--
--     여기서 @COUNT 가 나와야 한다. 안 나오면 세팅이 잘못된 것이지
--     시한 때문에 빠진 것이 아니다.  스케줄러 쿼리에서 시한 두 줄만 뺐다.
-- ─────────────────────────────────────────────────────────────
SELECT COUNT(*) AS 시한_빼면_걸리는_건수
FROM schedules_alarm a
JOIN schedules s      ON s.schedules_id = a.schedules_id
JOIN users_settings us ON us.users_id = s.users_id
WHERE s.title LIKE '만료테스트-%'
  AND a.status = 'PENDING'
  AND (a.next_attempt_at IS NULL OR a.next_attempt_at <= NOW())
  AND a.alarm_state = true
  AND s.status = 'ACTIVE'
  AND us.app_alarm_enabled = true;


-- ─────────────────────────────────────────────────────────────
-- [5] 실제 — 시한 조건까지 넣으면
--
--     0 이 나와야 한다.  스케줄러가 쓰는 조건 그대로다.
-- ─────────────────────────────────────────────────────────────
SELECT COUNT(*) AS 스케줄러가_집는_건수
FROM schedules_alarm a
JOIN schedules s      ON s.schedules_id = a.schedules_id
JOIN users_settings us ON us.users_id = s.users_id
WHERE s.title LIKE '만료테스트-%'
  AND a.status = 'PENDING'
  AND a.alarm_date_time <= NOW()
  AND a.alarm_date_time >= DATE_SUB(NOW(), INTERVAL 10 MINUTE)   -- 시한
  AND (a.next_attempt_at IS NULL OR a.next_attempt_at <= NOW())
  AND a.alarm_state = true
  AND s.status = 'ACTIVE'
  AND us.app_alarm_enabled = true;
