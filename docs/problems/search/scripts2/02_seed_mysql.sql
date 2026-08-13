-- [2단계] 통합검색 부하 테스트용 더미 데이터 (MySQL)
--
-- 개인 보관함 앱의 실제 사용 패턴에 맞춘 구성.
--
--            평균 유저 195명   헤비 유저 5명
--   보관함        10              30
--   일정          50             800
--   링크          80           1,200      북마크 대체 용도라 제일 많이 쌓인다
--   메모          30             500
--   파일          30             500
--   합계         200           3,030
--
--   전체 = 195×200 + 5×3,030 = 약 54,000행
--
-- 헤비 유저는 loadtest1~5 (users_id 가 가장 작은 5명), 나머지 195명은 평균 유저다.
--
-- 검색어는 'zzsearch'. 각 테이블에서 4행에만 심어 유저당 20건이 매칭된다.
-- 문자열은 실제에 가까운 길이로 채운다. 짧으면 행이 작아져 페이지당 행이 많아지고
-- 스캔 비용이 실제보다 싸게 나온다.
--
-- 선행: 00_numbers_mysql.sql, 01_create_users_mysql.sql
-- 재시드: 03_teardown_mysql.sql -> 01 -> 02
-- 주의: 멱등 아님. 두 번 돌리면 누적된다.

-- 헤비 유저 경계 (users_id 가 가장 작은 5명)
SET @heavy_max = (SELECT MIN(users_id) + 4 FROM users WHERE email LIKE 'loadtest%@toit.local');
SELECT @heavy_max AS heavy_users_up_to;

-- ═════════════════════════════════════════════════════════════
-- folders — 평균 10 / 헤비 30
-- ═════════════════════════════════════════════════════════════
INSERT INTO folders (name, memo, is_default, color, status, created_at, is_favorite, icon_idx, users_id)
SELECT CASE WHEN s.n <= 4
            THEN CONCAT('zzsearch 보관함 ', s.n, ' 정리용')
            ELSE CONCAT('보관함 ', s.n, ' ', SUBSTRING(MD5(CONCAT(u.users_id, s.n)), 1, 12)) END,
       CONCAT('메모 ', SUBSTRING(MD5(CONCAT('f', u.users_id, s.n)), 1, 20)),
       false, '#FFAA00', 'ACTIVE', NOW(), false, 0, u.users_id
FROM users u
JOIN seq_numbers s ON s.n <= (CASE WHEN u.users_id <= @heavy_max THEN 30 ELSE 10 END)
WHERE u.email LIKE 'loadtest%@toit.local';

-- ═════════════════════════════════════════════════════════════
-- schedules — 평균 50 / 헤비 800
-- ═════════════════════════════════════════════════════════════
INSERT INTO schedules (title, app_color, time_setting, start_date, end_date, status, created_at, users_id)
SELECT CASE WHEN s.n <= 4
            THEN CONCAT('zzsearch 일정 ', s.n, ' 회의 준비')
            ELSE CONCAT('일정 ', s.n, ' ', SUBSTRING(MD5(CONCAT('s', u.users_id, s.n)), 1, 20)) END,
       '#FFAA00', false, CURRENT_DATE, CURRENT_DATE, 'ACTIVE', NOW(), u.users_id
FROM users u
JOIN seq_numbers s ON s.n <= (CASE WHEN u.users_id <= @heavy_max THEN 800 ELSE 50 END)
WHERE u.email LIKE 'loadtest%@toit.local';

-- ═════════════════════════════════════════════════════════════
-- links — 평균 80 / 헤비 1,200
-- ═════════════════════════════════════════════════════════════
INSERT INTO links (links_name, links_url, links_thumbnail, storage_id, text_content, status, created_at, users_id)
SELECT CASE WHEN s.n <= 4
            THEN CONCAT('zzsearch 링크 ', s.n, ' 나중에 읽을 글')
            ELSE CONCAT('링크 ', s.n, ' ', SUBSTRING(MD5(CONCAT('l', u.users_id, s.n)), 1, 32)) END,
       CONCAT('https://example.com/article/', SUBSTRING(MD5(CONCAT(u.users_id, s.n)), 1, 16)),
       NULL,
       (SELECT MIN(folders_id) FROM folders WHERE users_id = u.users_id),
       CONCAT('설명 ', SUBSTRING(MD5(CONCAT('lc', u.users_id, s.n)), 1, 32)),
       'ACTIVE', NOW(), u.users_id
FROM users u
JOIN seq_numbers s ON s.n <= (CASE WHEN u.users_id <= @heavy_max THEN 1200 ELSE 80 END)
WHERE u.email LIKE 'loadtest%@toit.local';

-- ═════════════════════════════════════════════════════════════
-- texts — 평균 30 / 헤비 500
--   실제 메모는 길다. MD5 를 이어 붙여 100~140자로 만든다.
-- ═════════════════════════════════════════════════════════════
INSERT INTO texts (storage_id, text_content, status, created_at, users_id)
SELECT (SELECT MIN(folders_id) FROM folders WHERE users_id = u.users_id),
       CASE WHEN s.n <= 4
            THEN CONCAT('zzsearch 메모 ', s.n, ' ',
                        MD5(CONCAT('t1', u.users_id, s.n)), MD5(CONCAT('t2', u.users_id, s.n)),
                        MD5(CONCAT('t3', u.users_id, s.n)))
            ELSE CONCAT('메모 ', s.n, ' ',
                        MD5(CONCAT('t1', u.users_id, s.n)), MD5(CONCAT('t2', u.users_id, s.n)),
                        MD5(CONCAT('t3', u.users_id, s.n)), MD5(CONCAT('t4', u.users_id, s.n))) END,
       'ACTIVE', NOW(), u.users_id
FROM users u
JOIN seq_numbers s ON s.n <= (CASE WHEN u.users_id <= @heavy_max THEN 500 ELSE 30 END)
WHERE u.email LIKE 'loadtest%@toit.local';

-- ═════════════════════════════════════════════════════════════
-- attachments — 평균 30 / 헤비 500
-- ═════════════════════════════════════════════════════════════
INSERT INTO attachments (attachments_type, object_key, presigned_url, attachments_extension,
                         attachments_size, file_name, storage_id, text_content,
                         status, upload_status, created_at, users_id)
SELECT 'FILE',
       CONCAT('seed/', u.users_id, '/', SUBSTRING(MD5(CONCAT(u.users_id, s.n)), 1, 24)),
       CONCAT('https://example.com/', SUBSTRING(MD5(CONCAT('p', u.users_id, s.n)), 1, 16)),
       'pdf',
       ROUND(500000 + RAND() * 1000000),
       CASE WHEN s.n <= 4
            THEN CONCAT('zzsearch 자료 ', s.n, ' 정리본.pdf')
            ELSE CONCAT('문서 ', s.n, ' ', SUBSTRING(MD5(CONCAT('a', u.users_id, s.n)), 1, 20), '.pdf') END,
       (SELECT MIN(folders_id) FROM folders WHERE users_id = u.users_id),
       NULL, 'ACTIVE', 'CONFIRMED', NOW(), u.users_id
FROM users u
JOIN seq_numbers s ON s.n <= (CASE WHEN u.users_id <= @heavy_max THEN 500 ELSE 30 END)
WHERE u.email LIKE 'loadtest%@toit.local';

-- ═════════════════════════════════════════════════════════════
-- 확인 1. 테이블별 총 행 수
--   기대  folders 2,100 / schedules 13,750 / links 21,600 / texts 8,350 / attachments 8,350
-- ═════════════════════════════════════════════════════════════
SELECT 'folders' AS t, COUNT(*) AS seeded FROM folders     f JOIN users u ON f.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'schedules',   COUNT(*) FROM schedules   s JOIN users u ON s.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'links',       COUNT(*) FROM links       l JOIN users u ON l.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'texts',       COUNT(*) FROM texts       t JOIN users u ON t.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local'
UNION ALL SELECT 'attachments', COUNT(*) FROM attachments a JOIN users u ON a.users_id = u.users_id WHERE u.email LIKE 'loadtest%@toit.local';

-- ═════════════════════════════════════════════════════════════
-- 확인 2. 유저별 보유 행 수 — 헤비 3,030 / 평균 200
-- ═════════════════════════════════════════════════════════════
SELECT u.users_id, u.email,
       (SELECT COUNT(*) FROM folders     x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM schedules   x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM links       x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM texts       x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM attachments x WHERE x.users_id=u.users_id) AS total_rows
FROM users u WHERE u.email LIKE 'loadtest%@toit.local'
ORDER BY total_rows DESC LIMIT 3;

SELECT u.users_id, u.email,
       (SELECT COUNT(*) FROM folders     x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM schedules   x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM links       x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM texts       x WHERE x.users_id=u.users_id)
     + (SELECT COUNT(*) FROM attachments x WHERE x.users_id=u.users_id) AS total_rows
FROM users u WHERE u.email LIKE 'loadtest%@toit.local'
ORDER BY total_rows ASC LIMIT 3;

-- ═════════════════════════════════════════════════════════════
-- 확인 3. 검색어 매칭 — 각 4건, 합계 20건
-- ═════════════════════════════════════════════════════════════
SELECT u.users_id, u.email,
       (SELECT COUNT(*) FROM folders     x WHERE x.users_id=u.users_id AND x.status='ACTIVE' AND x.name         LIKE '%zzsearch%') AS folders,
       (SELECT COUNT(*) FROM schedules   x WHERE x.users_id=u.users_id AND x.status='ACTIVE' AND x.title        LIKE '%zzsearch%') AS schedules,
       (SELECT COUNT(*) FROM links       x WHERE x.users_id=u.users_id AND x.status='ACTIVE' AND x.links_name   LIKE '%zzsearch%') AS links,
       (SELECT COUNT(*) FROM texts       x WHERE x.users_id=u.users_id AND x.status='ACTIVE' AND x.text_content LIKE '%zzsearch%') AS texts,
       (SELECT COUNT(*) FROM attachments x WHERE x.users_id=u.users_id AND x.status='ACTIVE' AND x.file_name    LIKE '%zzsearch%') AS files
FROM users u WHERE u.email LIKE 'loadtest%@toit.local'
ORDER BY u.users_id LIMIT 3;

-- ═════════════════════════════════════════════════════════════
-- 확인 4. 폴더를 못 찾는 행 — 전부 0 이어야 한다
-- ═════════════════════════════════════════════════════════════
SELECT
  (SELECT COUNT(*) FROM links       x LEFT JOIN folders f ON f.folders_id=x.storage_id WHERE f.folders_id IS NULL) AS links_orphan,
  (SELECT COUNT(*) FROM texts       x LEFT JOIN folders f ON f.folders_id=x.storage_id WHERE f.folders_id IS NULL) AS texts_orphan,
  (SELECT COUNT(*) FROM attachments x LEFT JOIN folders f ON f.folders_id=x.storage_id WHERE f.folders_id IS NULL) AS attach_orphan;
