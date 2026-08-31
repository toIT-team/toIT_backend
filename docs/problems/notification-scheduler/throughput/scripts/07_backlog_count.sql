-- 20 ~ 25분 뒤에 실행한다. 1분 간격으로 두어 번 돌려 값이 안 변하면 끝난 것이다.

-- ─────────────────────────────────────────────────────────────
-- [1] 전체 — 홀딩은 다 나가고 유입 일부가 남아야 한다
-- ─────────────────────────────────────────────────────────────
SELECT
    CASE WHEN s.title LIKE '밀림홀딩-%' THEN '홀딩' ELSE '유입' END AS 구분,
    COUNT(*)                            AS 전체,
    SUM(a.status = 'SENT')              AS 발송됨,
    SUM(a.status = 'PENDING')           AS 대기중,
    SUM(a.status = 'FAILED')            AS 포기
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%'
GROUP BY 구분;


-- ─────────────────────────────────────────────────────────────
-- [2] 유입을 분별로 — 어느 분부터 잘렸는가
--
--     앞쪽 몇 분이 대기중으로 남고 뒤쪽은 발송됨이면 재현 성공이다.
--     경계가 곧 "밀림이 10분을 넘긴 지점" 이다.
-- ─────────────────────────────────────────────────────────────
SELECT
    SUBSTRING(s.title, 6, 2)            AS 몇분뒤,
    COUNT(*)                            AS 전체,
    SUM(a.status = 'SENT')              AS 발송됨,
    SUM(a.status = 'PENDING')           AS 대기중
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림유입-%'
GROUP BY 몇분뒤
ORDER BY 몇분뒤;


-- ─────────────────────────────────────────────────────────────
-- [3] 유실 — 시각이 지났는데 아직 PENDING 인 것
--
--     기록이 없다는 것도 같이 본다. FAILED 도 아니고 오류 코드도 없다.
-- ─────────────────────────────────────────────────────────────
SELECT
    COUNT(*)                                              AS 유실,
    MIN(a.alarm_date_time)                                AS 가장_오래된,
    MAX(a.alarm_date_time)                                AS 가장_최근,
    TIMESTAMPDIFF(MINUTE, MAX(a.alarm_date_time), NOW())  AS 최근것도_몇분_지남,
    SUM(a.attempt_count > 0)                              AS 시도한_적_있음,
    SUM(a.last_error_code IS NOT NULL)                    AS 오류코드_남음
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '밀림%'
  AND a.status = 'PENDING'
  AND a.alarm_date_time < NOW();


-- ─────────────────────────────────────────────────────────────
-- [4] 얼마나 걸렸는가 — 홀딩을 비우는 데 든 시간
-- ─────────────────────────────────────────────────────────────
SELECT
    MIN(n.sent_at)                                        AS 첫_발송,
    MAX(n.sent_at)                                        AS 마지막_발송,
    TIMESTAMPDIFF(SECOND, MIN(n.sent_at), MAX(n.sent_at)) AS 드레인_초,
    COUNT(*)                                              AS 보낸_건수
FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '밀림%' AND n.sent_at IS NOT NULL;
