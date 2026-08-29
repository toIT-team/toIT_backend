-- 재처리 구조를 배포하기 전에 한 번 실행한다.
--
-- ddl-auto=update 는 컬럼을 더해주지만, NOT NULL 컬럼을 기존 행이 있는 테이블에
-- 붙이려다 실패한다. 기본값을 주고 미리 만들어 두면 애플리케이션이 그대로 뜬다.
--
-- 순서
--   1  이 파일을 실행한다
--   2  애플리케이션을 배포한다
--
-- 되돌리려면 맨 아래 주석을 참고한다.


-- 1. 예약 테이블에 컬럼 넷을 더한다
ALTER TABLE schedules_alarm
    ADD COLUMN status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN last_error_code VARCHAR(50) NULL,
    ADD COLUMN attempt_count   INT         NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at DATETIME(6) NULL;

-- 2. 이미 보낸 것은 SENT 로 옮긴다
--    안 옮기면 지난 알림이 전부 PENDING 이 되어 한꺼번에 다시 나간다.
UPDATE schedules_alarm SET status = 'SENT' WHERE is_sent = TRUE;

-- 3. 알림함에 멱등키
--    NULL 은 유니크 제약에 걸리지 않으므로 기존 행은 그대로 둔다.
ALTER TABLE user_notification
    ADD COLUMN idempotency_key VARCHAR(100) NULL,
    ADD UNIQUE KEY uk_user_notification_idem (idempotency_key);

-- 4. is_sent 를 뗀다
--    엔티티에서 빠졌으므로 애플리케이션이 이 컬럼을 채우지 않는다.
--    NOT NULL 인 채로 두면 INSERT 가 실패한다.
ALTER TABLE schedules_alarm DROP COLUMN is_sent;


-- 확인
SELECT status, COUNT(*) FROM schedules_alarm GROUP BY status;

-- 이미 idx_alarm_pending 을 만들었다면 지운다. 지금 규모에서 효과가
-- 측정되지 않아 넣지 않기로 했다. 자세한 것은 9장에 있다.
-- DROP INDEX idx_alarm_pending ON schedules_alarm;


-- 되돌리기
--
-- ALTER TABLE schedules_alarm ADD COLUMN is_sent BOOLEAN NOT NULL DEFAULT FALSE;
-- UPDATE schedules_alarm SET is_sent = TRUE WHERE status = 'SENT';
-- ALTER TABLE schedules_alarm
--     DROP COLUMN status, DROP COLUMN last_error_code,
--     DROP COLUMN attempt_count, DROP COLUMN next_attempt_at;
-- ALTER TABLE user_notification
--     DROP INDEX uk_user_notification_idem, DROP COLUMN idempotency_key;
