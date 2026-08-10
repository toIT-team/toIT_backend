-- [정리] loadtest 더미 데이터 + 유저 30명 전체 삭제 (MySQL)
--
-- 원본: 03_teardown_dbeaver.sql (PostgreSQL)
-- 변환: DELETE ... USING → DELETE t FROM t JOIN u ON ...
--
-- 사용법: 전체 실행.
-- 'loadtest%@toit.local' 유저와 그 유저들의 모든 행을 지운다. 기존 1·2번은 건드리지 않는다.

-- 1) loadtest 유저들의 데이터 먼저 삭제 (FK 때문에 유저보다 먼저)
DELETE a FROM attachments a JOIN users u ON a.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';
DELETE t FROM texts       t JOIN users u ON t.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';
DELETE l FROM links       l JOIN users u ON l.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';
DELETE s FROM schedules   s JOIN users u ON s.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';
DELETE f FROM folders     f JOIN users u ON f.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';

-- 2) 유저 계정 삭제
DELETE FROM users WHERE email LIKE 'loadtest%@toit.local';

-- 확인: 0 이 나와야 한다
SELECT COUNT(*) AS remaining_loadtest_users FROM users WHERE email LIKE 'loadtest%@toit.local';
