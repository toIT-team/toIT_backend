-- 예비 측정 — 가짜 토큰 한 건이 몇 ms 걸리는지, 어떤 오류 코드가 오는지 본다.
--
-- 왜 필요한가
--   밀려서 만료에 이르는 과정을 재현하려면 스케줄러를 1분 넘게 붙잡아야 한다.
--   붙잡는 시간은 "한 건에 몇 ms 냐" 로 정해지는데, 가짜 토큰이 얼마나 걸리는지
--   모른다. FCM 이 형식만 보고 바로 거절하면 성공(330ms)보다 빠를 수도 있다.
--   그 값을 알아야 본 테스트에 몇 건을 넣을지 계산할 수 있다.
--
-- 왜 가짜 토큰인가
--   FCM 왕복은 진짜로 일어나고 폰에는 아무것도 안 온다. 진짜 토큰으로 수천 건을
--   돌리면 테스트 기기가 감당을 못 한다.
--
-- 왜 전용 사용자인가
--   본인 계정의 토큰을 지웠다 되돌리면 실수하기 쉽다. 아예 다른 사용자를 만들어
--   거기에만 가짜 토큰을 단다. 본인 토큰은 건드리지 않는다.
--
-- 보는 것
--   [FCM] 전송실패 ... code=?     어떤 오류인지
--   발송시도 → 재시도예약 간격     한 건에 몇 ms 인지
--
-- 주의
--   UNREGISTERED 가 오면 코드가 그 토큰을 지운다. 그러면 두 번째 알림부터는
--   FCM 을 안 타고 29ms 로 끝난다. 로그에서 [FCM] 줄이 한 번만 나오면 그 경우다.


SET @COUNT = 50;


-- ─────────────────────────────────────────────────────────────
-- [1] 전용 사용자 — 없으면 만든다
-- ─────────────────────────────────────────────────────────────
INSERT INTO users (email, name, role, status, auth_provider, provider_users_id, created_at)
SELECT 'fcmprobe@toit.local', 'fcm probe', 'ROLE_USER', 'ACTIVE', 'KAKAO', 'fcm-probe', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'fcmprobe@toit.local');

SET @PROBE_ID = (SELECT users_id FROM users WHERE email = 'fcmprobe@toit.local');

-- 알림 설정이 켜져 있어야 조회에 걸린다
INSERT INTO users_settings (users_id, app_alarm_enabled)
SELECT @PROBE_ID, true
WHERE NOT EXISTS (SELECT 1 FROM users_settings WHERE users_id = @PROBE_ID);

UPDATE users_settings SET app_alarm_enabled = true WHERE users_id = @PROBE_ID;


-- ─────────────────────────────────────────────────────────────
-- [2] 가짜 토큰 — 형식만 흉내낸다
-- ─────────────────────────────────────────────────────────────
DELETE FROM fcm_token WHERE users_id = @PROBE_ID;

INSERT INTO fcm_token (users_id, fcm_token, last_updated_at)
VALUES (@PROBE_ID,
        'fMkQ8yTgS0aBcDeFgHiJkL:APA91bF4KET0KENpr0be000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000',
        NOW());


-- ─────────────────────────────────────────────────────────────
-- [3] 앞 라운드 정리
-- ─────────────────────────────────────────────────────────────
DELETE n FROM user_notification n
JOIN schedules s ON s.schedules_id = n.target_id
WHERE s.title LIKE '가짜토큰-%';

DELETE a FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '가짜토큰-%';

DELETE FROM schedules WHERE title LIKE '가짜토큰-%';


-- ─────────────────────────────────────────────────────────────
-- [4] 알림 @COUNT 건 — 다음 틱에 나가도록 지금 시각으로
-- ─────────────────────────────────────────────────────────────
INSERT INTO schedules (
    title, app_color, folders_id, time_setting,
    start_date, end_date, start_time, end_time,
    status, memo, created_at, users_id
)
SELECT
    CONCAT('가짜토큰-', LPAD(nums.n, 4, '0')),
    'blue300', NULL, true,
    CURDATE(), CURDATE(), '23:00:00', '23:30:00',
    'ACTIVE', '', NOW(), @PROBE_ID
FROM (
    SELECT a.d + b.d * 10 + c.d * 100 + 1 AS n
    FROM (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) a
    CROSS JOIN
         (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) b
    CROSS JOIN
         (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) c
) nums
WHERE nums.n <= @COUNT;

INSERT INTO schedules_alarm (
    schedules_id, alarm_state, alarm_date_time, alarm_offset_minutes,
    status, attempt_count, is_read
)
SELECT s.schedules_id, true, NOW(), 5, 'PENDING', 0, false
FROM schedules s
WHERE s.users_id = @PROBE_ID
  AND s.title LIKE '가짜토큰-%';


-- ─────────────────────────────────────────────────────────────
-- [5] 확인 — 다음 틱에 이만큼 집힌다
-- ─────────────────────────────────────────────────────────────
SELECT @PROBE_ID AS 전용_사용자, COUNT(*) AS 대기중
FROM schedules_alarm a
JOIN schedules s ON s.schedules_id = a.schedules_id
WHERE s.title LIKE '가짜토큰-%' AND a.status = 'PENDING';
