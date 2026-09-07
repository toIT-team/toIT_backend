-- 만료 테스트 데이터를 지운다. FK 때문에 순서를 지켜야 한다.

DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '만료테스트-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '만료테스트-%';

DELETE FROM schedules WHERE title LIKE '만료테스트-%';

-- 확인 — 셋 다 0 이어야 한다
SELECT
    (SELECT COUNT(*) FROM schedules WHERE title LIKE '만료테스트-%')       AS 일정,
    (SELECT COUNT(*) FROM schedules_alarm a
       JOIN schedules s ON s.schedules_id = a.schedules_id
      WHERE s.title LIKE '만료테스트-%')                                   AS 알림예약,
    (SELECT COUNT(*) FROM user_notification n
       JOIN schedules s ON s.schedules_id = n.target_id
      WHERE s.title LIKE '만료테스트-%')                                   AS 알림함;
