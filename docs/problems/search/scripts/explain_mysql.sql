-- 통합검색 인덱스가 MySQL 8.0 에서도 같은 효과인지 확인
--
-- PostgreSQL 기준 값 (README-postgresql.md 3장)
--   Seq Scan → Bitmap Index Scan
--   읽고 버린 행 39,005 → 990
--   읽은 페이지 534 → 22
--   쿼리 시간 5.1ms → 0.68ms
--
-- MySQL 에는 Bitmap Index Scan 이 없다. ref 로 잡히는지, rows 가 얼마인지를 본다.
--
-- 사용법: @v_uid 를 부하 테스트 대상 유저로 바꾸고 전체 실행

SET @v_uid = 1;
SET @v_kw  = 'loadtest';

-- ─────────────────────────────────────────────────────────────
-- 0. 인덱스가 실제로 만들어져 있는지
-- ─────────────────────────────────────────────────────────────
SELECT TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME, CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND INDEX_NAME LIKE 'idx_%_users_status%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 통계가 오래됐으면 실행 계획이 엉뚱하게 나온다. 먼저 갱신한다.
ANALYZE TABLE folders, schedules, links, texts, attachments;

-- ─────────────────────────────────────────────────────────────
-- 1. 전체 행 수 / 대상 유저 행 수
--    인덱스가 좁혀주는 비율을 알기 위한 분모
-- ─────────────────────────────────────────────────────────────
SELECT 'schedules' AS t, COUNT(*) AS total,
       SUM(users_id = @v_uid AND status = 'ACTIVE') AS mine FROM schedules
UNION ALL SELECT 'links',  COUNT(*), SUM(users_id = @v_uid AND status = 'ACTIVE') FROM links
UNION ALL SELECT 'texts',  COUNT(*), SUM(users_id = @v_uid AND status = 'ACTIVE') FROM texts
UNION ALL SELECT 'attachments', COUNT(*), SUM(users_id = @v_uid AND status = 'ACTIVE') FROM attachments
UNION ALL SELECT 'folders', COUNT(*), SUM(users_id = @v_uid AND status = 'ACTIVE') FROM folders;

-- ─────────────────────────────────────────────────────────────
-- 2. EXPLAIN — 인덱스를 타는지 (type, key, rows 를 본다)
--    type=ref, key=idx_..., rows 가 유저 행 수에 가까우면 정상
--    type=ALL, key=NULL 이면 풀스캔 — PostgreSQL 의 개선이 안 살아난 것
-- ─────────────────────────────────────────────────────────────
EXPLAIN
SELECT * FROM schedules
WHERE users_id = @v_uid AND status = 'ACTIVE'
  AND lower(title) LIKE lower(CONCAT('%', @v_kw, '%'));

EXPLAIN
SELECT * FROM links
WHERE users_id = @v_uid AND status = 'ACTIVE'
  AND lower(links_name) LIKE lower(CONCAT('%', @v_kw, '%'));

EXPLAIN
SELECT * FROM texts
WHERE users_id = @v_uid AND status = 'ACTIVE'
  AND lower(text_content) LIKE lower(CONCAT('%', @v_kw, '%'));

EXPLAIN
SELECT * FROM folders
WHERE users_id = @v_uid AND status = 'ACTIVE'
  AND lower(name) LIKE lower(CONCAT('%', @v_kw, '%'));

-- attachments 는 인덱스가 (users_id, status, attachments_type) 3컬럼이다
EXPLAIN
SELECT * FROM attachments
WHERE users_id = @v_uid AND status = 'ACTIVE' AND attachments_type = 'FILE'
  AND lower(file_name) LIKE lower(CONCAT('%', @v_kw, '%'));

-- 실제 애플리케이션 쿼리는 folders 와 JOIN 한다 (N+1 제거 후 구조)
EXPLAIN
SELECT a.*, f.name
FROM attachments a
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = @v_uid AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND lower(a.file_name) LIKE lower(CONCAT('%', @v_kw, '%'))
ORDER BY a.created_at DESC;

-- ─────────────────────────────────────────────────────────────
-- 3. EXPLAIN ANALYZE — 실제로 몇 행을 읽고 얼마나 걸렸는지
--    MySQL 8.0.18+ 에서만 동작. PostgreSQL 의 "Rows Removed by Filter" 에 해당하는 값을
--    직접 주지 않으므로, 인덱스가 넘긴 행 수 - 최종 행 수 로 읽는다.
-- ─────────────────────────────────────────────────────────────
EXPLAIN ANALYZE
SELECT * FROM schedules
WHERE users_id = @v_uid AND status = 'ACTIVE'
  AND lower(title) LIKE lower(CONCAT('%', @v_kw, '%'));

EXPLAIN ANALYZE
SELECT a.*, f.name
FROM attachments a
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = @v_uid AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND lower(a.file_name) LIKE lower(CONCAT('%', @v_kw, '%'))
ORDER BY a.created_at DESC;

-- ─────────────────────────────────────────────────────────────
-- 4. 인덱스를 껐을 때와 비교 — 개선 폭을 숫자로 만들기 위한 대조군
--    IGNORE INDEX 로 강제 풀스캔시켜 rows 와 시간을 비교한다.
-- ─────────────────────────────────────────────────────────────
EXPLAIN ANALYZE
SELECT * FROM schedules IGNORE INDEX (idx_schedules_users_status)
WHERE users_id = @v_uid AND status = 'ACTIVE'
  AND lower(title) LIKE lower(CONCAT('%', @v_kw, '%'));

-- ─────────────────────────────────────────────────────────────
-- 5. 실행 계획을 옵티마이저가 왜 그렇게 골랐는지 (선택)
-- ─────────────────────────────────────────────────────────────
-- SET optimizer_trace = 'enabled=on';
-- SELECT * FROM schedules WHERE users_id = @v_uid AND status = 'ACTIVE'
--   AND lower(title) LIKE lower(CONCAT('%', @v_kw, '%'));
-- SELECT TRACE FROM information_schema.OPTIMIZER_TRACE\G
-- SET optimizer_trace = 'enabled=off';
