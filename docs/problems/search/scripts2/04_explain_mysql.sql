-- 통합검색 쿼리의 실행 계획 수집
--
-- 인덱스 없는 상태와 복합 인덱스 상태에서 각각 돌려 비교한다.
--   PART A   양쪽 상태에서 공통으로 돌린다
--   PART B   복합 인덱스가 있을 때만 (IGNORE INDEX 대조군)
--
-- 인덱스 없는 상태에서 PART B 를 돌리면 에러가 난다.
--   ERROR 1176: Key 'idx_schedules_users_status' doesn't exist in table
--
-- DBeaver 는 블록 선택 후 Alt+X. 결과를 하나씩 보려면 Ctrl+Enter.


-- ═════════════════════════════════════════════════════════════
-- 0. 대상 확인 — 아래 쿼리의 100 / 65 를 여기 결과로 맞출 것
--    헤비 유저는 loadtest 유저 중 앞 5명이다.
-- ═════════════════════════════════════════════════════════════

SELECT u.users_id, u.email,
       (SELECT COUNT(*) FROM schedules x WHERE x.users_id = u.users_id) AS schedules,
       CASE WHEN u.users_id <= (SELECT MIN(users_id) + 4 FROM users
                                WHERE email LIKE 'loadtest%@toit.local')
            THEN '헤비' ELSE '평균' END AS 유형
FROM users u
WHERE u.email LIKE 'loadtest%@toit.local'
  AND u.users_id IN (
      (SELECT MIN(users_id)      FROM users WHERE email LIKE 'loadtest%@toit.local'),
      (SELECT MIN(users_id) + 35 FROM users WHERE email LIKE 'loadtest%@toit.local')
  )
ORDER BY u.users_id;

-- 통계 갱신. 안 하면 실행 계획이 엉뚱하게 나온다.
ANALYZE TABLE folders, schedules, links, texts, attachments;

-- 전체 행 수 (기대: folders 2,102 / schedules 13,750 / links 21,603
--                texts 8,351 / attachments 8,355 — 합계 54,161)
SELECT
  (SELECT COUNT(*) FROM folders)     AS folders,
  (SELECT COUNT(*) FROM schedules)   AS schedules,
  (SELECT COUNT(*) FROM links)       AS links,
  (SELECT COUNT(*) FROM texts)       AS texts,
  (SELECT COUNT(*) FROM attachments) AS attachments;


-- ═════════════════════════════════════════════════════════════
-- PART A-1. EXPLAIN — 5개 테이블이 인덱스를 타는지
--   인덱스 없음   type=ALL,  key=NULL
--   복합 인덱스   type=ref,  key=idx_..., rows ≈ 그 유저 행 수
-- ═════════════════════════════════════════════════════════════

EXPLAIN SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE'
  AND title LIKE '%zzsearch%';

EXPLAIN SELECT * FROM links
WHERE users_id = 100 AND status = 'ACTIVE'
  AND links_name LIKE '%zzsearch%';

EXPLAIN SELECT * FROM texts
WHERE users_id = 100 AND status = 'ACTIVE'
  AND text_content LIKE '%zzsearch%';

EXPLAIN SELECT * FROM folders
WHERE users_id = 100 AND status = 'ACTIVE'
  AND name LIKE '%zzsearch%';

EXPLAIN SELECT * FROM attachments
WHERE users_id = 100 AND status = 'ACTIVE' AND attachments_type = 'FILE'
  AND file_name LIKE '%zzsearch%';

-- 실제 애플리케이션 쿼리 (N+1 제거 후 구조: folders 와 JOIN)
EXPLAIN SELECT a.*, f.name
FROM attachments a
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = 100 AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND a.file_name LIKE '%zzsearch%'
ORDER BY a.created_at DESC;


-- ═════════════════════════════════════════════════════════════
-- PART A-2. EXPLAIN ANALYZE — 실제로 몇 행을 읽고 몇 ms 걸리는지
--   글에 인용할 대표 출력이다. 두 상태에서 각각 받아 둘 것.
-- ═════════════════════════════════════════════════════════════

-- 평균 유저
EXPLAIN ANALYZE SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE'
  AND title LIKE '%zzsearch%';

-- 헤비 유저 — 데이터가 16배일 때
EXPLAIN ANALYZE SELECT * FROM schedules
WHERE users_id = 65 AND status = 'ACTIVE'
  AND title LIKE '%zzsearch%';

-- JOIN 쿼리 (애플리케이션이 실제로 쓰는 것)
EXPLAIN ANALYZE SELECT a.*, f.name
FROM attachments a
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = 100 AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND a.file_name LIKE '%zzsearch%'
ORDER BY a.created_at DESC;


-- ═════════════════════════════════════════════════════════════
-- PART B. 복합 인덱스 상태에서만 — 같은 쿼리를 인덱스 없이 강제 실행
--   인덱스를 지웠다 붙였다 하지 않고 같은 세션에서 비교할 수 있다.
--   ⚠ 인덱스 없는 상태에서 돌리면 ERROR 1176
-- ═════════════════════════════════════════════════════════════

EXPLAIN ANALYZE SELECT * FROM schedules IGNORE INDEX (idx_schedules_users_status)
WHERE users_id = 100 AND status = 'ACTIVE'
  AND title LIKE '%zzsearch%';

EXPLAIN ANALYZE SELECT * FROM schedules IGNORE INDEX (idx_schedules_users_status)
WHERE users_id = 65 AND status = 'ACTIVE'
  AND title LIKE '%zzsearch%';

EXPLAIN ANALYZE SELECT a.*, f.name
FROM attachments a IGNORE INDEX (idx_attachments_users_status)
JOIN folders f ON f.folders_id = a.storage_id
WHERE a.users_id = 100 AND a.status = 'ACTIVE' AND a.attachments_type = 'FILE'
  AND a.file_name LIKE '%zzsearch%'
ORDER BY a.created_at DESC;
