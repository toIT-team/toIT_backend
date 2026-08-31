-- 가짜 토큰 테스트 정리. 전용 사용자는 남겨둔다(다시 쓸 수 있게).
-- 사용자까지 지우려면 맨 아래 주석을 푼다.

SET @PROBE_ID = (SELECT users_id FROM users WHERE email = 'fcmprobe@toit.local');

DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '가짜토큰-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '가짜토큰-%';

DELETE FROM schedules WHERE title LIKE '가짜토큰-%';

DELETE FROM fcm_token WHERE users_id = @PROBE_ID;

-- 확인 — 넷 다 0 이어야 한다
SELECT
    (SELECT COUNT(*) FROM schedules WHERE title LIKE '가짜토큰-%')        AS 일정,
    (SELECT COUNT(*) FROM schedules_alarm a
       JOIN schedules s ON s.schedules_id = a.schedules_id
      WHERE s.title LIKE '가짜토큰-%')                                    AS 알림예약,
    (SELECT COUNT(*) FROM user_notification n
       JOIN schedules s ON s.schedules_id = n.target_id
      WHERE s.title LIKE '가짜토큰-%')                                    AS 알림함,
    (SELECT COUNT(*) FROM fcm_token WHERE users_id = @PROBE_ID)           AS 가짜토큰;


-- 전용 사용자까지 지우려면
-- DELETE FROM users_settings WHERE users_id = @PROBE_ID;
-- DELETE FROM users WHERE users_id = @PROBE_ID;
