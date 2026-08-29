-- 측정이 끝나면 만든 데이터를 전부 지운다.
-- 제목으로만 고르므로 원래 일정은 건드리지 않는다.

-- 1. 알림함 기록
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '유실테스트-%';

-- 2. 알림 예약
DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';

-- 3. 일정
DELETE FROM schedules WHERE title LIKE '유실테스트-%';

-- 4. 지표 스냅샷 (05_metrics.sql 이 만든 것)
DROP TABLE IF EXISTS metrics_snapshot;

-- 5. 남은 게 없는지 확인
SELECT COUNT(*) AS 남은_일정 FROM schedules WHERE title LIKE '유실테스트-%';
