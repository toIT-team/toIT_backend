-- 스케줄러를 두세 바퀴 돌린 뒤에 실행한다.
--
-- 만료된 알림에 아무 일도 안 일어났다면 3.2 의 "조용히 사라진다" 가 증명된다.
-- 무엇 하나라도 값이 바뀌어 있으면 어딘가 기록이 남는다는 뜻이다.

-- ─────────────────────────────────────────────────────────────
-- [1] 상태가 그대로인가
--
--     기대값 :  대기중 = 200,  나머지 전부 0
-- ─────────────────────────────────────────────────────────────
SELECT
    COUNT(*)                              AS 전체,
    SUM(a.status = 'PENDING')             AS 대기중,
    SUM(a.status = 'SENT')                AS 발송됨,
    SUM(a.status = 'FAILED')              AS 포기,
    SUM(a.attempt_count > 0)              AS 시도한_적_있음,
    SUM(a.last_error_code IS NOT NULL)    AS 오류코드_남은_건수,
    SUM(a.next_attempt_at IS NOT NULL)    AS 재시도_예약된_건수
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '만료테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [2] 알림함에 줄이 생겼는가
--
--     기대값 :  0
--     알림함 줄은 발송을 시도할 때 만들어진다. 0 이면 시도조차 안 한 것이다.
-- ─────────────────────────────────────────────────────────────
SELECT COUNT(*) AS 알림함_줄
FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '만료테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [3] 얼마나 오래 이 상태인가
--
--     시간이 지날수록 지난_분 만 늘어나고 상태는 안 바뀐다.
--     이 값이 20분, 30분이 되어도 그대로면 "영영" 이 확인된다.
-- ─────────────────────────────────────────────────────────────
SELECT
    MIN(a.alarm_date_time)                                AS 울렸어야_할_시각,
    NOW()                                                 AS 지금,
    TIMESTAMPDIFF(MINUTE, MIN(a.alarm_date_time), NOW())  AS 지난_분
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '만료테스트-%';
