-- 통합검색 인덱스가 MySQL 8.0 에서도 같은 효과인지 확인
--
-- 원본 PostgreSQL 측정 (README-postgresql.md 3장)
--   Seq Scan → Bitmap Index Scan
--   읽고 버린 행 39,005 → 990
--   읽은 페이지 534 → 22
--   쿼리 시간 5.1ms → 0.68ms  (7.5배)
--
-- MySQL 에는 Bitmap Index Scan 이 없다. 세컨더리 인덱스에서 PK 를 얻어
-- PK 트리를 다시 타는 구조(북마크 조회)라 결과가 다를 수 있다.
--
-- 대상 (02_seed_mysql.sql 실행 후 기준, users_id 는 환경마다 다르니 먼저 확인할 것)
--   일반 유저 34   1,000행 중 10건 매칭
--   헤비 유저 63  10,000행 중 10건 매칭
--
-- DBeaver 에서는 문장 하나씩(Ctrl+Enter) 실행하는 게 결과를 보기 편하다.
-- 전체 실행은 Alt+X.

-- ═════════════════════════════════════════════════════════════
-- 0. 대상 유저 확인 — 아래 34 / 63 을 여기 나온 값으로 바꿀 것
-- ═════════════════════════════════════════════════════════════
SELECT u.users_id, u.email,
       (SELECT COUNT(*) FROM schedules x WHERE x.users_id = u.users_id) AS schedules
FROM users u
WHERE u.email IN ('loadtest1@toit.local', 'loadtest30@toit.local')
ORDER BY u.users_id;

-- 통계 갱신. 안 하면 실행 계획이 엉뚱하게 나온다.
ANALYZE TABLE folders, schedules, links, texts, attachments;

-- 전체 행 수 (기대: schedules·links·texts 39,000 / attachments 34,000 / folders 3,000)
SELECT
  (SELECT COUNT(*) FROM folders)     AS folders,
  (SELECT COUNT(*) FROM schedules)   AS schedules,
  (SELECT COUNT(*) FROM links)       AS links,
  (SELECT COUNT(*) FROM texts)       AS texts,
  (SELECT COUNT(*) FROM attachments) AS attachments;

-- ═════════════════════════════════════════════════════════════
-- 1. EXPLAIN — 인덱스를 타는지
--    type=ref, key=idx_..., rows ≈ 그 유저 행 수 면 정상
--    type=ALL, key=NULL 이면 풀스캔
-- ═════════════════════════════════════════════════════════════

EXPLAIN SELECT * FROM schedules
WHERE users_id = 34 AND status = 'ACTIVE'
  AND lower(title) LIKE '%zzsearch%';

EXPLAIN SELECT * FROM links
WHERE users_id = 34 AND status = 'ACTIVE'
  AND lower(links_name) LIKE '%zzsearch%';

EXPLAIN SELECT * FROM texts
WHERE users_id = 34 AND status = 'ACTIVE'
  AND lower(text_content) LIKE '%zzsearch%';

EXPLAIN SELECT * FROM folders
WHERE users_id = 34 AND status = 'ACTIVE'
  AND lower(name) LIKE '%zzsearch%';

EXPLAIN SELECT * FROM attachments
WHERE users_id = 34 AND status = 'ACTIVE' AND attachments_type = 'FILE'
  AND lower(file_name) LIKE '%zzsearch%';

-- 실제 애플리케이션 쿼리 (N+1 제거 후 구조: folders 와 JOIN)
EXPLAIN SELECT a.*, f.name
FROM attachments a
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = 34 AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND lower(a.file_name) LIKE '%zzsearch%'
ORDER BY a.created_at DESC;

-- ═════════════════════════════════════════════════════════════
-- 2. 일반 유저(34) — 인덱스 vs 강제 풀스캔
--    같은 쿼리를 두 번. actual time 을 비교한다.
-- ═════════════════════════════════════════════════════════════

-- 2-1 인덱스 사용
EXPLAIN ANALYZE SELECT * FROM schedules
WHERE users_id = 34 AND status = 'ACTIVE'
  AND lower(title) LIKE '%zzsearch%';

-- 2-2 인덱스 무시 (대조군)
EXPLAIN ANALYZE SELECT * FROM schedules IGNORE INDEX (idx_schedules_users_status)
WHERE users_id = 34 AND status = 'ACTIVE'
  AND lower(title) LIKE '%zzsearch%';

-- ═════════════════════════════════════════════════════════════
-- 3. 헤비 유저(63) — 데이터가 10배일 때
-- ═════════════════════════════════════════════════════════════

-- 3-1 인덱스 사용
EXPLAIN ANALYZE SELECT * FROM schedules
WHERE users_id = 63 AND status = 'ACTIVE'
  AND lower(title) LIKE '%zzsearch%';

-- 3-2 인덱스 무시
EXPLAIN ANALYZE SELECT * FROM schedules IGNORE INDEX (idx_schedules_users_status)
WHERE users_id = 63 AND status = 'ACTIVE'
  AND lower(title) LIKE '%zzsearch%';

-- ═════════════════════════════════════════════════════════════
-- 4. JOIN 쿼리 (실제 애플리케이션이 쓰는 것)
-- ═════════════════════════════════════════════════════════════

EXPLAIN ANALYZE SELECT a.*, f.name
FROM attachments a
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = 34 AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND lower(a.file_name) LIKE '%zzsearch%'
ORDER BY a.created_at DESC;

EXPLAIN ANALYZE SELECT a.*, f.name
FROM attachments a IGNORE INDEX (idx_attachments_users_status)
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = 34 AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND lower(a.file_name) LIKE '%zzsearch%'
ORDER BY a.created_at DESC;
