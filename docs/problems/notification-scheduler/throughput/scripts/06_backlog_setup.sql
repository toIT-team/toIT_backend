-- 밀려서 유실에 이르는 과정을 재현한다.
--
-- 먼저 할 일
--   **기기에서 앱 알림을 꺼둔다.** 서버는 평소처럼 보내고 FCM 도 성공을 돌려주지만
--   화면에는 안 뜬다. users_settings 를 끄면 조회에서 걸러져 발송 자체가 안 되니
--   그쪽이 아니라 OS 알림을 꺼야 한다.
--
-- 왜 500건으로는 안 되나
--   조회에 LIMIT 가 없어서 한 번에 다 집어간다. 500건이 199초 걸려도 그 실행 안에서
--   전부 나간다. 유실은 "집히기 전에 방치된 것" 에만 생긴다.
--
-- 그래서 두 덩이로 나눈다
--   홀딩   2,500건을 지금 시각으로.  스케줄러를 15분 안팎 붙잡는다
--   유입     200건을 1~20분 뒤로 10건씩.  붙잡힌 동안 도착해 방치된다
--
--   방치된 것 중 10분을 넘긴 것이 조회에서 빠진다. 그게 유실이다.
--
-- 예상
--   홀딩 2,500 / 150건분 = 약 16분
--   16분째 틱의 oldest 는 6분.  1~6분에 도착한 유입 60건이 창 밖
--
-- 실제 발송량
--   약 2,600건.  기기당 한도(분당 240 · 시간당 5,000) 안쪽이다.
--
-- 순서
--   1  이 파일 실행
--   2  20 ~ 25분 기다린다.  그동안 로그를 받아둔다
--   3  07_backlog_count.sql
--   4  08_backlog_teardown.sql

SET @USERS_ID = 32;      -- 본인 것으로 바꾼다. 토큰이 살아 있어야 한다
SET @HOLD     = 2500;    -- 스케줄러를 붙잡을 덩이
SET @FEED_MIN = 20;      -- 몇 분에 걸쳐 유입시킬지
SET @FEED_PER = 10;      -- 분당 유입 건수


-- ─────────────────────────────────────────────────────────────
-- [0] 앞 라운드 정리
-- ─────────────────────────────────────────────────────────────
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '밀림%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%';

DELETE FROM schedules WHERE title LIKE '밀림%';


-- ─────────────────────────────────────────────────────────────
-- [1] 홀딩 — 지금 시각.  다음 틱에 통째로 집힌다
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('밀림홀딩-', LPAD(nums.n, 5, '0')),
    'blue300', NULL, true,
    CURDATE(), CURDATE(), '23:00:00', '23:30:00',
    'ACTIVE', '', NOW(), @USERS_ID
FROM (
    SELECT a.d + b.d * 10 + c.d * 100 + d.d * 1000 + 1 AS n
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
    CROSS JOIN
         (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) d
) nums
WHERE nums.n <= @HOLD;

INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT s.schedules_id, true, NOW(), 5, 'PENDING', 0, false
FROM schedules s
WHERE s.users_id = @USERS_ID AND s.title LIKE '밀림홀딩-%';


-- ─────────────────────────────────────────────────────────────
-- [2] 유입 — 1분 뒤부터 @FEED_PER 건씩
--     제목 뒤 숫자가 곧 몇 분 뒤인지다.  나중에 어느 분이 잘렸는지 센다
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('밀림유입-', LPAD(CEIL(nums.n / @FEED_PER), 2, '0'), '분-', LPAD(nums.n, 4, '0')),
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
WHERE nums.n <= @FEED_MIN * @FEED_PER;

-- 제목에 박아둔 분을 그대로 시각으로 쓴다
INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT
    s.schedules_id,
    true,
    -- '밀림유입-' 이 5글자라 숫자는 6번째부터다. MySQL 의 SUBSTRING 은
    -- 바이트가 아니라 글자로 센다.
    DATE_ADD(NOW(), INTERVAL CAST(SUBSTRING(s.title, 6, 2) AS UNSIGNED) MINUTE),
    5,
    'PENDING',
    0,
    false
FROM schedules s
WHERE s.users_id = @USERS_ID AND s.title LIKE '밀림유입-%';


-- ─────────────────────────────────────────────────────────────
-- [3] 확인
-- ─────────────────────────────────────────────────────────────
SELECT
    SUM(s.title LIKE '밀림홀딩-%')  AS 홀딩,
    SUM(s.title LIKE '밀림유입-%')  AS 유입,
    MIN(a.alarm_date_time)          AS 가장_이른_시각,
    MAX(a.alarm_date_time)          AS 가장_늦은_시각
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%';
