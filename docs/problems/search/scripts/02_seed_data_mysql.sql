-- [2단계] loadtest 유저 전원에게 더미 데이터 시드 (MySQL)
--
-- 원본: 02_seed_data_dbeaver.sql (PostgreSQL)
-- 변환: DO $$ + FOR LOOP → users 와 seq_numbers 의 CROSS JOIN (집합 연산 한 번으로 처리)
--       generate_series → seq_numbers,  || → CONCAT,  random() → RAND()
--
-- 선행: 00_numbers_mysql.sql, 01_create_users_mysql.sql
--
-- 도메인 제약 반영:
--   folders     : 유저당 최대 100개 (FoldersService) → LEAST(@v_n, 100)
--   attachments : 총 5GB 용량 제한 → 평균 1MB 가정 시 행 수 상한 ~5,000 → LEAST(@v_n, 5000)
--                 (검색은 파일 바이트를 안 읽으므로 실제 5GB 생성 불필요. size 컬럼만 현실값)
--   schedules/links/texts : 제한 없음 → @v_n 그대로
--
-- 사용법: @v_n 만 정하고 전체 실행. (현실선 1000 권장)
-- 주의: 멱등 아님. 다시 돌리면 누적됨(재시드 전 03_teardown_mysql.sql 권장).

SET @v_n = 1000;   -- 유저·테이블당 행 수

-- folders (유저당 최대 100)
INSERT INTO folders (name, memo, is_default, color, status, created_at, is_favorite, icon_idx, users_id)
SELECT CONCAT('loadtest folder ', s.n), 'seed', false, '#FFAA00', 'ACTIVE', NOW(), false, 0, u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= LEAST(@v_n, 100)
WHERE u.email LIKE 'loadtest%@toit.local';

-- schedules (제한 없음)
INSERT INTO schedules (title, app_color, time_setting, start_date, end_date, status, created_at, users_id)
SELECT CONCAT('loadtest schedule ', s.n), '#FFAA00', false, CURRENT_DATE, CURRENT_DATE, 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.email LIKE 'loadtest%@toit.local';

-- links (제한 없음)
INSERT INTO links (links_name, links_url, links_thumbnail, storage_id, text_content, status, created_at, users_id)
SELECT CONCAT('loadtest link ', s.n), CONCAT('https://example.com/', s.n), NULL, 1,
       CONCAT('loadtest content ', s.n), 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.email LIKE 'loadtest%@toit.local';

-- texts (제한 없음)
INSERT INTO texts (storage_id, text_content, status, created_at, users_id)
SELECT 1, CONCAT('loadtest text ', s.n), 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.email LIKE 'loadtest%@toit.local';

-- attachments (5GB 캡 → 최대 5,000행, size 는 평균 ~1MB 랜덤: 0.5MB~1.5MB)
INSERT INTO attachments (attachments_type, object_key, presigned_url, attachments_extension,
                         attachments_size, file_name, storage_id, text_content, status, created_at, users_id)
SELECT 'FILE', CONCAT('seed/key/', s.n), CONCAT('https://example.com/', s.n), 'pdf',
       ROUND(500000 + RAND() * 1000000), CONCAT('loadtest file ', s.n, '.pdf'), 1, NULL, 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= LEAST(@v_n, 5000)
WHERE u.email LIKE 'loadtest%@toit.local';

-- 확인
SELECT 'folders' AS t, COUNT(*) AS rows_seeded FROM folders     f JOIN users u ON f.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'schedules',   COUNT(*) FROM schedules   s JOIN users u ON s.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'links',       COUNT(*) FROM links       l JOIN users u ON l.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'texts',       COUNT(*) FROM texts       t JOIN users u ON t.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'attachments', COUNT(*) FROM attachments a JOIN users u ON a.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';
