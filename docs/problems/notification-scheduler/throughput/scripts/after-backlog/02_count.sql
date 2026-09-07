-- 25분 뒤에 실행한다. 1분 간격으로 두어 번 돌려 값이 안 변하면 끝난 것이다.
--
-- 07_backlog_count.sql 과 다른 점은 EXPIRED 열이다.
-- 개선 전에는 유실이 PENDING 인 채로 남았지만, 이제 EXPIRED 로 표시된다.
-- 그 열을 안 보면 유실을 놓친다.

-- ─────────────────────────────────────────────────────────────
-- [1] 전체 — 홀딩도 유입도 전부 SENT 여야 한다
--
--     개선 전 : 홀딩 2,500 SENT · 유입 170 SENT + 30 PENDING(유실)
--     개선 후 : 둘 다 전부 SENT · EXPIRED 0
-- ─────────────────────────────────────────────────────────────
SELECT
    CASE WHEN s.title LIKE '밀림홀딩-%' THEN '홀딩' ELSE '유입' END AS 구분,
    COUNT(*)                            AS 전체,
    SUM(a.status = 'SENT')              AS 발송됨,
    SUM(a.status = 'PENDING')           AS 대기중,
    SUM(a.status = 'EXPIRED')           AS 만료됨,
    SUM(a.status = 'FAILED')            AS 포기
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%'
GROUP BY 구분;


-- ─────────────────────────────────────────────────────────────
-- [2] 유입을 분별로 — 개선 전에는 앞 3분이 잘렸다
--
--     개선 전 : 01 · 02 · 03분이 대기중,  04분부터 발송됨
--     개선 후 : 전부 발송됨
-- ─────────────────────────────────────────────────────────────
SELECT
    SUBSTRING(s.title, 6, 2)            AS 몇분뒤,
    COUNT(*)                            AS 전체,
    SUM(a.status = 'SENT')              AS 발송됨,
    SUM(a.status = 'EXPIRED')           AS 만료됨
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림유입-%'
GROUP BY 몇분뒤
ORDER BY 몇분뒤;


-- ─────────────────────────────────────────────────────────────
-- [3] 얼마나 걸렸는가 — 홀딩을 비우는 데 든 시간
--
--     개선 전 770.7초(12.8분) → 10분 창을 넘겨 유실
--     개선 후 190초 안팎(3.1분) 이 나와야 한다
--     이보다 길면 기기 제한(분당 240)에 걸려 재시도가 섞인 것이다 → [4] 를 본다
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
WHERE n.type = 'SCHEDULE' AND s.title LIKE '밀림홀딩-%' AND n.sent_at IS NOT NULL;


-- ─────────────────────────────────────────────────────────────
-- [4] 기기별로 — FCM 제한에 걸렸는지 본다
--
--     한 사람 몫만 포기가 몰리면 그 기기가 분당 240건에 걸린 것이다
-- ─────────────────────────────────────────────────────────────
SELECT
    s.users_id                  AS 사용자,
    COUNT(*)                    AS 전체,
    SUM(a.status = 'SENT')      AS 발송됨,
    SUM(a.status = 'EXPIRED')   AS 만료됨,
    SUM(a.status = 'FAILED')    AS 포기,
    GROUP_CONCAT(DISTINCT a.last_error_code) AS 오류코드
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%'
GROUP BY s.users_id;
