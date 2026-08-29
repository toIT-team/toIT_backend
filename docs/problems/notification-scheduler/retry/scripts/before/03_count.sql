-- 라운드가 끝난 뒤 결과를 센다. 1분 간격으로 몇 번 돌려 값이 변하는지 본다.
-- 값이 안 변하면 그 알림들은 영영 안 간다는 뜻이다.

-- 1. 발송 상태
SELECT
    COUNT(*)                        AS 전체,
    SUM(a.is_sent = true)           AS 발송됨,
    SUM(a.is_sent = false)          AS 미발송
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';

-- 2. 유실 건수 — 울릴 시각이 지났는데 아직 안 보낸 것
SELECT COUNT(*) AS 유실
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%'
  AND a.is_sent = false
  AND a.alarm_date_time < NOW();

-- 3. 알림함 기준 — 시도와 성공을 나눠 본다
--    알림함 줄은 발송 전에 만들어지므로 전체 건수가 곧 시도 횟수다.
--    sent_at 은 성공했을 때만 채워진다.
SELECT
    COUNT(*)                        AS 시도,
    SUM(n.sent_at IS NOT NULL)      AS 성공,
    SUM(n.sent_at IS NULL)          AS 실패
FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '유실테스트-%';

-- 4. 드레인 시간 — 밀린 것을 비우는 데 얼마나 걸렸는지
--    개선 후 재측정에서 이 값이 처리량 글의 출발점이 된다.
SELECT
    MIN(n.sent_at)                                        AS 첫_발송,
    MAX(n.sent_at)                                        AS 마지막_발송,
    TIMESTAMPDIFF(SECOND, MIN(n.sent_at), MAX(n.sent_at)) AS 드레인_초
FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '유실테스트-%'
  AND n.sent_at IS NOT NULL;
