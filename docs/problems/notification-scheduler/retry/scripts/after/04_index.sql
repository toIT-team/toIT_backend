-- 인덱스가 실제로 일을 하는지 잰다.
--
-- 알림을 만들되 전부 SENT 로 둔다. 스케줄러는 매분 조회를 하지만 보낼 것이
-- 없으므로 **푸시가 한 건도 안 나간다.** 인덱스가 하는 일은 "만 건 중에서
-- PENDING 을 골라내는 것" 이고, 골라낸 게 0건이어도 고르는 비용은 똑같이 든다.
--
-- 500건으로는 차이가 안 보인다. 유휴 기준선이 초당 40행이라 2분이면 4,800행인데,
-- 500행짜리 스캔은 거기에 묻힌다. 10,000건은 되어야 갈린다.
--
--
-- 순서
--
--   1  이 파일의 [1] 을 실행한다        앞 라운드를 지우고 10,000건을 SENT 로 만든다
--   2  05_metrics.sql → 2분 유휴 → 05_metrics.sql     인덱스 있음
--   3  이 파일의 [2] 를 실행한다        인덱스를 뗀다
--   4  05_metrics.sql → 2분 유휴 → 05_metrics.sql     인덱스 없음
--   5  이 파일의 [3] 을 실행한다        인덱스를 되돌린다
--   6  04_teardown.sql                 데이터 정리
--
-- 2번과 4번의 "읽은 행" 차이가 곧 인덱스 효과다.
-- 2분 유휴에 스케줄러가 두어 번 도므로, 인덱스가 없으면 그때마다 10,000행을 훑는다.
--
-- 주의 : 유휴 구간이어야 하므로 그 사이에 아무도 앱을 쓰지 않게 한다.


-- ─────────────────────────────────────────────────────────────
-- [1] 데이터 준비 — 10,000건을 만들고 전부 SENT 로 둔다
-- ─────────────────────────────────────────────────────────────

SET @USERS_ID = 32;
SET @COUNT    = 10000;

-- 앞 라운드가 남아 있으면 유니크 키(schedules_id)에 걸린다. 먼저 지운다.
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '유실테스트-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';

DELETE FROM schedules WHERE title LIKE '유실테스트-%';

-- 0~9 네 벌을 교차시켜 1~10000 을 만든다.
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('유실테스트-', LPAD(nums.n, 5, '0')),
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
WHERE nums.n <= @COUNT;

-- 전부 SENT 로 둔다. 그래야 조회는 돌되 발송은 안 나간다.
INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT s.schedules_id, true, NOW(), 0, 'SENT', 0, false
FROM schedules s
WHERE s.users_id = @USERS_ID
  AND s.title LIKE '유실테스트-%'
  AND NOT EXISTS (SELECT 1 FROM schedules_alarm a WHERE a.schedules_id = s.schedules_id);

-- 확인 — PENDING 이 0 이어야 푸시가 안 나간다
SELECT
    COUNT(*)                    AS 전체,
    SUM(a.status = 'SENT')      AS 발송됨,
    SUM(a.status = 'PENDING')   AS 대기중
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [2] 인덱스를 뗀다
-- ─────────────────────────────────────────────────────────────
DROP INDEX idx_alarm_pending ON schedules_alarm;
--
-- EXPLAIN 으로 확인 — type 이 ALL 로 바뀌어야 한다
EXPLAIN SELECT * FROM schedules_alarm
WHERE status = 'PENDING' AND alarm_date_time <= NOW();


-- ─────────────────────────────────────────────────────────────
-- [3] 인덱스를 되돌린다
-- ─────────────────────────────────────────────────────────────
CREATE INDEX idx_alarm_pending ON schedules_alarm (status, alarm_date_time);

EXPLAIN SELECT * FROM schedules_alarm
 WHERE status = 'PENDING' AND alarm_date_time <= NOW();
