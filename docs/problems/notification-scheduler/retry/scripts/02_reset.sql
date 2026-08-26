-- 매 라운드 시작 전에 실행한다. 알림 시각을 한 시각으로 모으고 상태를 초기화한다.
-- 개선 전과 개선 후의 조건을 똑같이 맞추기 위한 스크립트다.
--
-- 실험 A : 서버를 먼저 끈 뒤 NOW() 로 실행한다. 아무도 못 보는 사이에 시각이 지나간다.
-- 실험 B : 서버는 켜둔 채 DATE_ADD(NOW(), INTERVAL 2 MINUTE) 로 실행한다.

UPDATE schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
SET a.alarm_date_time = DATE_ADD(NOW(), INTERVAL 2 MINUTE),                             -- 실험 B 는 여기를 +2분으로
    a.alarm_state = true,
    a.is_sent = false,
    a.is_read = false
WHERE s.title LIKE '유실테스트-%';

-- 지난 라운드에서 만들어진 알림함 기록도 지운다.
-- 남겨두면 이번 라운드에서 몇 건이 새로 생겼는지 셀 수 없다.
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '유실테스트-%';

SELECT
    COUNT(*)                AS 전체,
    MIN(a.alarm_date_time)  AS 울릴_시각
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '유실테스트-%';
