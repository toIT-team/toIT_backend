-- 라운드가 끝났는지 보고, 결과를 센다.
-- 1분 간격으로 두어 번 돌려 값이 안 변하면 끝난 것이다.

-- ─────────────────────────────────────────────────────────────
-- [1] 다 나갔는가
--
--     대기중이 0 이어야 한 라운드가 온전히 끝난 것이다.
--     남아 있으면 창(10분)을 넘겨 잘린 것이다 → 그 N 은 감당 못 한 것
-- ─────────────────────────────────────────────────────────────
SELECT
    COUNT(*)                    AS 전체,
    SUM(a.status = 'SENT')      AS 발송됨,
    SUM(a.status = 'PENDING')   AS 대기중,
    SUM(a.status = 'FAILED')    AS 포기,
    SUM(a.attempt_count > 1)    AS 재시도한_것
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '속도테스트-%';


-- ─────────────────────────────────────────────────────────────
-- [2] 얼마나 걸렸는가 — 이게 이 테스트의 답이다
--
--     드레인_초 = 첫 발송에서 마지막 발송까지.
--     분당 = 건수 ÷ 드레인_초 × 60.  N 을 올릴 때 이 값이 따라 오르면 성공
-- ─────────────────────────────────────────────────────────────
SELECT
    COUNT(*)                                              AS 보낸_건수,
    MIN(n.sent_at)                                        AS 첫_발송,
    MAX(n.sent_at)                                        AS 마지막_발송,
    TIMESTAMPDIFF(SECOND, MIN(n.sent_at), MAX(n.sent_at)) AS 드레인_초,
    ROUND(COUNT(*) /
        NULLIF(TIMESTAMPDIFF(SECOND, MIN(n.sent_at), MAX(n.sent_at)), 0) * 60)
                                                          AS 분당
FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '속도테스트-%' AND n.sent_at IS NOT NULL;


-- ─────────────────────────────────────────────────────────────
-- [3] 기기별로 — FCM 제한에 걸렸는지 본다
--
--     한 사람 몫만 실패가 몰리면 그 기기가 분당 240건에 걸린 것이다.
--     그러면 그 N 의 측정치는 못 쓴다
-- ─────────────────────────────────────────────────────────────
SELECT
    s.users_id                  AS 사용자,
    COUNT(*)                    AS 전체,
    SUM(a.status = 'SENT')      AS 발송됨,
    SUM(a.status = 'PENDING')   AS 대기중,
    SUM(a.status = 'FAILED')    AS 포기,
    GROUP_CONCAT(DISTINCT a.last_error_code) AS 오류코드
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '속도테스트-%'
GROUP BY s.users_id;


-- ─────────────────────────────────────────────────────────────
-- [4] 분당 몇 건씩 나갔는가 — 고르게 나갔는지 본다
--
--     중간에 뚝 떨어지는 분이 있으면 그 지점을 로그에서 찾아본다
-- ─────────────────────────────────────────────────────────────
SELECT
    DATE_FORMAT(n.sent_at, '%H:%i')  AS 분,
    COUNT(*)                         AS 건수
FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '속도테스트-%' AND n.sent_at IS NOT NULL
GROUP BY 분
ORDER BY 분;
