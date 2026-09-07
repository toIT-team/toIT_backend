-- 4장에서 유실을 재현했던 그 시나리오를 개선 후에 다시 돌린다.
--
-- 입력은 같고 결과만 달라져야 한다.
--   개선 전   2,500건에 12.8분  →  유입 200건 중 30건 유실
--   개선 후   2,500건에  3.1분  →  유실 0
--
-- 06_backlog_setup.sql 과 다른 점은 하나다. 세 사람에게 나눠 보낸다.
--   개선 전은 분당 195건이라 한 기기로 받을 수 있었다(FCM 제한 240).
--   N=4 면 분당 798건이라 한 기기로는 못 받는다.
--
-- ⚠ 3대면 기기당 분당 266건이라 제한 240 을 살짝 넘는다
--   그래도 돌린다. 넘으면 FCM 이 그 건을 거절하고, 우리 코드는 FAILED 로 보고
--   1분 뒤 재시도한다. 유실이 되지는 않고 드레인 시간만 늘어난다.
--   걸렸는지는 02_count.sql 의 [4] 기기별 표에서 확인한다.
--   시간당 제한(5,000)은 기기당 900건이라 여유가 있다.
--
-- 먼저 할 일
--   1  세 기기 모두 OS 알림을 꺼둔다.  2,700건이 나간다 (기기당 900건)
--      users_settings 를 끄면 조회에서 걸러져 발송 자체가 안 되므로 그쪽이 아니다
--   2  alarm.send.concurrency = 4 인지 확인
--   3  로그에 [ALARM] 만료 · 실행완료 줄이 보이는지 확인 (새 코드가 떴다는 신호)
--
-- 순서
--   1  이 파일 실행
--   2  25분 기다린다.  그동안 로그를 받아둔다
--   3  02_count.sql
--   4  ../08_backlog_teardown.sql
--
-- 예상
--   홀딩 2,500 / 798건분 = 3.1분  →  10분 창 안
--   유입 200건 전부 발송.  유실 0

SET @U1       = 32;      -- IOS 18.6
SET @U2       = 33;      -- IOS 26.6.1
SET @U3       = 265;     -- ANDROID 16
SET @HOLD     = 2500;    -- 스케줄러를 붙잡을 덩이
SET @FEED_MIN = 20;      -- 몇 분에 걸쳐 유입시킬지
SET @FEED_PER = 10;      -- 분당 유입 건수


-- ─────────────────────────────────────────────────────────────
-- [0] 앞 라운드 정리
-- ─────────────────────────────────────────────────────────────
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE n.type = 'SCHEDULE' AND s.title LIKE '밀림%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%';

DELETE FROM schedules WHERE title LIKE '밀림%';


-- ─────────────────────────────────────────────────────────────
-- [1] 홀딩 — 지금 시각으로 @HOLD 건.  세 사람에게 번갈아
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
    'ACTIVE', '', NOW(),
    CASE nums.n % 3 WHEN 0 THEN @U1 WHEN 1 THEN @U2 ELSE @U3 END
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
WHERE s.title LIKE '밀림홀딩-%';


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
    'ACTIVE', '', NOW(),
    CASE nums.n % 3 WHEN 0 THEN @U1 WHEN 1 THEN @U2 ELSE @U3 END
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
WHERE s.title LIKE '밀림유입-%';


-- ─────────────────────────────────────────────────────────────
-- [3] 확인 — 세 사람에게 고르게 갔는지, 토큰이 있는지
-- ─────────────────────────────────────────────────────────────
SELECT
    CASE WHEN s.title LIKE '밀림홀딩-%' THEN '홀딩' ELSE '유입' END      AS 구분,
    s.users_id                                                          AS 사용자,
    COUNT(*)                                                            AS 건수,
    (SELECT COUNT(*) FROM fcm_token t WHERE t.users_id = s.users_id)     AS 토큰수,
    (SELECT us.app_alarm_enabled FROM users_settings us
      WHERE us.users_id = s.users_id)                                    AS 알림설정
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%'
GROUP BY 구분, s.users_id
ORDER BY 구분, s.users_id;

-- 토큰수가 0이거나 알림설정이 0이면 그 사람 몫은 FCM 을 안 타서 측정이 어긋난다.
