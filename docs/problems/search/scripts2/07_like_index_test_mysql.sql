-- 검색 컬럼 단독 인덱스가 LIKE '%키워드%' 를 태울 수 있는지 확인 (테스트 서버 전용)
--
-- 확인하려는 것
--   검색 컬럼에 인덱스를 걸면 통합검색이 그 인덱스를 쓰는가
--
-- 앞선 (users_id, status, title) 테스트는 users_id 가 이미 범위를 좁혀버려
-- "검색 컬럼 인덱스가 되는가" 의 답이 되지 못한다. 여기서는 users_id 를 빼고
-- 검색 컬럼만 남겨 순수하게 본다.
--
-- 접두 검색(LIKE 'x%')은 기능이 달라져 채택하지 않으므로 측정 대상에서 뺐다.
--
-- DBeaver 는 Alt+X (Execute script).


-- ═════════════════════════════════════════════════════════════
-- STEP 0. 이전 실험 인덱스가 남아 있는지 확인 — 남아 있으면 결과가 오염된다
-- ═════════════════════════════════════════════════════════════

SELECT INDEX_NAME FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'schedules'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;


-- ═════════════════════════════════════════════════════════════
-- STEP 1. 검색 컬럼 단독 인덱스 생성
-- ═════════════════════════════════════════════════════════════

CREATE INDEX tmp_title_only ON schedules (title);
ANALYZE TABLE schedules;


-- ═════════════════════════════════════════════════════════════
-- STEP 2. 인덱스를 타는지 확인
--   type / key / key_len / rows 를 볼 것
--   기대: type=ALL, key=NULL — 만들어도 안 쓴다
-- ═════════════════════════════════════════════════════════════

EXPLAIN SELECT * FROM schedules WHERE title LIKE '%zzsearch%';

EXPLAIN ANALYZE SELECT * FROM schedules WHERE title LIKE '%zzsearch%';

-- 애플리케이션 실제 쿼리 형태 (users_id, status 조건 포함)
EXPLAIN SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE' AND title LIKE '%zzsearch%';


-- ═════════════════════════════════════════════════════════════
-- STEP 3. 원상복구 — 반드시 실행할 것
--   남겨두면 이후 측정에서 옵티마이저가 이 인덱스를 후보로 삼는다.
-- ═════════════════════════════════════════════════════════════

DROP INDEX tmp_title_only ON schedules;
ANALYZE TABLE schedules;

-- 확인 — tmp_ 로 시작하는 인덱스가 없어야 한다
SELECT INDEX_NAME FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'schedules'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
