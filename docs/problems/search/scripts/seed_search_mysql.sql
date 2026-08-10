-- /page/search 부하 테스트용 더미 데이터 시드 (MySQL)
--
-- 원본: seed_search_dbeaver.sql (PostgreSQL)
-- 변환: DO $$ 블록 → 순수 SQL, generate_series → seq_numbers, || → CONCAT
--
-- 선행: 00_numbers_mysql.sql
--
-- 사용법:
--   1) 아래 @v_uid, @v_n 두 값만 바꾼다.
--        @v_uid = 더미 데이터를 넣을 기존 users_id
--        @v_n   = schedules/links/texts/attachments 각 테이블 생성 행 수
--                 (folders 는 앱 상한 100개에 맞춰 LEAST(@v_n, 100) 으로만 생성)
--   2) 전체 실행.
--   3) 유저 1·2번 각각 하려면 @v_uid 만 바꿔서 두 번 실행.
--
-- 주의: 멱등 아님. 같은 uid로 두 번 돌리면 행이 2배가 된다.
-- 검색 키워드 "loadtest" 로 검색하면 매칭 행 전부가 응답에 직렬화됨(최악 부하, LIMIT 없음).

SET @v_uid = 1;       -- ← 대상 users_id
SET @v_n   = 10000;   -- ← 테이블당 행 수

-- 대상 유저 존재 확인 (0 이면 아래 INSERT 는 전부 0행이 들어간다)
SELECT COUNT(*) AS target_user_exists FROM users WHERE users_id = @v_uid;

-- folders (검색 컬럼: name) — 유저당 최대 100개 상한에 맞춰 캡
INSERT INTO folders (name, memo, is_default, color, status, created_at, is_favorite, icon_idx, users_id)
SELECT CONCAT('loadtest folder ', s.n), 'seed', false, '#FFAA00', 'ACTIVE', NOW(), false, 0, u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= LEAST(@v_n, 100)
WHERE u.users_id = @v_uid;

-- schedules (검색 컬럼: title)
INSERT INTO schedules (title, app_color, time_setting, start_date, end_date, status, created_at, users_id)
SELECT CONCAT('loadtest schedule ', s.n), '#FFAA00', false, CURRENT_DATE, CURRENT_DATE, 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.users_id = @v_uid;

-- links (검색 컬럼: links_name) — storage_id 는 검색에 영향 없음(폴더 조인 X)
INSERT INTO links (links_name, links_url, links_thumbnail, storage_id, text_content, status, created_at, users_id)
SELECT CONCAT('loadtest link ', s.n), CONCAT('https://example.com/', s.n), NULL, 1,
       CONCAT('loadtest content ', s.n), 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.users_id = @v_uid;

-- texts (검색 컬럼: text_content)
INSERT INTO texts (storage_id, text_content, status, created_at, users_id)
SELECT 1, CONCAT('loadtest text ', s.n), 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.users_id = @v_uid;

-- attachments (검색 컬럼: file_name, FILE 타입만 검색됨)
INSERT INTO attachments (attachments_type, object_key, presigned_url, attachments_extension,
                         attachments_size, file_name, storage_id, text_content, status, created_at, users_id)
SELECT 'FILE', CONCAT('seed/key/', s.n), CONCAT('https://example.com/', s.n), 'pdf',
       1024, CONCAT('loadtest file ', s.n, '.pdf'), 1, NULL, 'ACTIVE', NOW(), u.users_id
FROM users u
         JOIN seq_numbers s ON s.n <= @v_n
WHERE u.users_id = @v_uid;

-- 확인
SELECT 'folders' AS t, COUNT(*) AS rows_for_user FROM folders     WHERE users_id = @v_uid
UNION ALL SELECT 'schedules',   COUNT(*) FROM schedules   WHERE users_id = @v_uid
UNION ALL SELECT 'links',       COUNT(*) FROM links       WHERE users_id = @v_uid
UNION ALL SELECT 'texts',       COUNT(*) FROM texts       WHERE users_id = @v_uid
UNION ALL SELECT 'attachments', COUNT(*) FROM attachments WHERE users_id = @v_uid;
