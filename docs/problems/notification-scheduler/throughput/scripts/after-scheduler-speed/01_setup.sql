-- 동시성을 올려가며 처리 속도를 잰다.
--
-- 왜 세 사람에게 나누나
--   FCM 은 한 기기에 분당 240건까지만 받는다. 한 사람에게 몰아 보내면 N=2 부터
--   그 제한에 걸려, 우리 코드가 아니라 FCM 의 스로틀링을 재게 된다.
--   기기 셋으로 나누면 분당 720건까지 안전하다.
--
--   N = 1   분당 150건   기기당  50건
--   N = 2   분당 300건   기기당 100건
--   N = 4   분당 600건   기기당 200건
--   N = 8   분당 1,200건 기기당 400건   ← 초과.  실제 FCM 으로는 못 잰다
--
-- 먼저 할 일
--   세 사람 다 기기 알림을 꺼둔다. 라운드마다 1,000건이 나간다.
--
-- 순서
--   1  application.properties 의 alarm.send.concurrency 를 바꾸고 재시작
--   2  이 파일 실행
--   3  다 나갈 때까지 기다린다 (N=1 이면 7분쯤)
--   4  02_count.sql
--   5  로그를 받아 06_latency.py
--   6  03_teardown.sql
--   7  N 을 바꿔 1번부터 반복

SET @U1     = 32;      -- IOS 18.6
SET @U2     = 33;      -- IOS 26.6.1
SET @U3     = 265;     -- ANDROID 16
SET @COUNT  = 1000;


-- ─────────────────────────────────────────────────────────────
-- [0] 앞 라운드 정리
-- ─────────────────────────────────────────────────────────────
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '속도테스트-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '속도테스트-%';

DELETE FROM schedules WHERE title LIKE '속도테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [1] 일정 @COUNT 건 — 세 사람에게 번갈아 배정
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('속도테스트-', LPAD(nums.n, 5, '0')),
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
WHERE nums.n <= @COUNT;


-- ─────────────────────────────────────────────────────────────
-- [2] 알림 예약 — 전부 지금 시각.  다음 실행에 통째로 집힌다
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT s.schedules_id, true, NOW(), 5, 'PENDING', 0, false
FROM schedules s
WHERE s.title LIKE '속도테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [3] 확인 — 세 사람에게 고르게 갔는지, 토큰이 있는지
-- ─────────────────────────────────────────────────────────────
SELECT
    s.users_id                                                        AS 사용자,
    COUNT(*)                                                          AS 알림수,
    (SELECT COUNT(*) FROM fcm_token t WHERE t.users_id = s.users_id)   AS 토큰수,
    (SELECT us.app_alarm_enabled FROM users_settings us
      WHERE us.users_id = s.users_id)                                  AS 알림설정
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '속도테스트-%'
GROUP BY s.users_id;

-- 토큰수가 0이거나 알림설정이 0이면 그 사람 몫은 FCM 을 안 타서 측정이 어긋난다.
