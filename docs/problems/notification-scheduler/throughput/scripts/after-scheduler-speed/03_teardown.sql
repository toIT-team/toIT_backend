-- 라운드가 끝나면 지운다. 다음 N 으로 넘어가기 전에 반드시 실행한다.
-- 안 지우면 다음 라운드 조회에 이번 것이 섞인다.

DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '속도테스트-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '속도테스트-%';

DELETE FROM schedules WHERE title LIKE '속도테스트-%';


-- 남은 게 없는지 확인. 셋 다 0 이어야 한다.
SELECT
    (SELECT COUNT(*) FROM schedules WHERE title LIKE '속도테스트-%')       AS 일정,
    (SELECT COUNT(*) FROM schedules_alarm a
       JOIN schedules s ON s.schedules_id = a.schedules_id
      WHERE s.title LIKE '속도테스트-%')                                   AS 알림,
    (SELECT COUNT(*) FROM user_notification n
       JOIN schedules s ON s.schedules_id = n.target_id
      WHERE s.title LIKE '속도테스트-%')                                   AS 알림함;
