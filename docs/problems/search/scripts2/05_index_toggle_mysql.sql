-- 통합검색 인덱스 켜고 끄기 — 부하 테스트의 단계별 상태를 만든다
--
-- 세 단계를 비교하는 것이 목적이다.
--   ① 인덱스 없음
--   ② 복합 인덱스 (users_id, status)
--   ③ 커버링 인덱스 (users_id, status, 검색컬럼)
--
-- ⚠ 인덱스만 지우려 하면 이 에러가 난다.
--     Cannot drop index '...': needed in a foreign key constraint
--   InnoDB 는 FK 컬럼에 인덱스를 반드시 요구하고, (users_id, status) 의 맨 앞이
--   users_id 라 이 인덱스가 FK 인덱스 역할을 겸하고 있다.
--   그래서 ① 을 만들려면 FK 를 잠시 떼야 한다.
--
-- ⚠ 테스트 서버 전용. 운영에서 실행하지 말 것.
-- ⚠ spring.jpa.hibernate.ddl-auto=update 이므로 인덱스를 지운 뒤 앱을 재시작하면
--   JPA 가 @Index 를 보고 다시 만든다. 측정 중에는 앱을 재시작하지 말 것.
--
-- DBeaver 에서는 Alt+X (Execute script). 블록 단위로 나눠 실행한다.

-- ═════════════════════════════════════════════════════════════
-- 0. 현재 FK 와 인덱스 확인 — 여기서 나온 이름을 아래에서 쓴다
-- ═════════════════════════════════════════════════════════════

-- 대상 테이블의 FK 목록
SELECT k.TABLE_NAME, k.CONSTRAINT_NAME, k.COLUMN_NAME,
       k.REFERENCED_TABLE_NAME, k.REFERENCED_COLUMN_NAME,
       r.DELETE_RULE, r.UPDATE_RULE
FROM information_schema.KEY_COLUMN_USAGE k
JOIN information_schema.REFERENTIAL_CONSTRAINTS r
     ON r.CONSTRAINT_SCHEMA = k.CONSTRAINT_SCHEMA
    AND r.CONSTRAINT_NAME   = k.CONSTRAINT_NAME
WHERE k.TABLE_SCHEMA = DATABASE()
  AND k.TABLE_NAME IN ('folders','schedules','links','texts','attachments')
  AND k.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY k.TABLE_NAME, k.CONSTRAINT_NAME;

-- ─────────────────────────────────────────────────────────────
-- 복구용 DDL 을 미리 뽑아 둔다. 결과를 복사해서 따로 저장할 것.
-- ─────────────────────────────────────────────────────────────
SELECT CONCAT(
         'ALTER TABLE ', k.TABLE_NAME,
         ' ADD CONSTRAINT ', k.CONSTRAINT_NAME,
         ' FOREIGN KEY (', k.COLUMN_NAME, ')',
         ' REFERENCES ', k.REFERENCED_TABLE_NAME, ' (', k.REFERENCED_COLUMN_NAME, ')',
         CASE WHEN r.DELETE_RULE <> 'NO ACTION' THEN CONCAT(' ON DELETE ', r.DELETE_RULE) ELSE '' END,
         CASE WHEN r.UPDATE_RULE <> 'NO ACTION' THEN CONCAT(' ON UPDATE ', r.UPDATE_RULE) ELSE '' END,
         ';'
       ) AS restore_fk_ddl
FROM information_schema.KEY_COLUMN_USAGE k
JOIN information_schema.REFERENTIAL_CONSTRAINTS r
     ON r.CONSTRAINT_SCHEMA = k.CONSTRAINT_SCHEMA
    AND r.CONSTRAINT_NAME   = k.CONSTRAINT_NAME
WHERE k.TABLE_SCHEMA = DATABASE()
  AND k.TABLE_NAME IN ('folders','schedules','links','texts','attachments')
  AND k.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY k.TABLE_NAME;

-- 삭제용 DDL
SELECT CONCAT('ALTER TABLE ', TABLE_NAME, ' DROP FOREIGN KEY ', CONSTRAINT_NAME, ';') AS drop_fk_ddl
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('folders','schedules','links','texts','attachments')
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- 현재 인덱스
SHOW INDEX FROM schedules;


-- ═════════════════════════════════════════════════════════════
-- ① 인덱스 없는 상태 만들기
--    위 drop_fk_ddl 결과를 여기 붙여넣고 실행한 뒤 인덱스를 지운다
-- ═════════════════════════════════════════════════════════════

-- (1) FK 제거 — 0번에서 뽑은 drop_fk_ddl 을 붙여넣을 것
-- ALTER TABLE schedules DROP FOREIGN KEY <이름>;
-- ...

-- (2) 인덱스 제거
-- ALTER TABLE schedules   DROP INDEX idx_schedules_users_status;
-- ALTER TABLE links       DROP INDEX idx_links_users_status;
-- ALTER TABLE texts       DROP INDEX idx_texts_users_status;
-- ALTER TABLE folders     DROP INDEX idx_folders_users_status;
-- ALTER TABLE attachments DROP INDEX idx_attachments_users_status;

-- (3) 확인 — remaining 이 0, EXPLAIN 이 type=ALL / key=NULL 이어야 한다
-- SELECT COUNT(*) AS remaining FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%_users_status%';
--
-- EXPLAIN SELECT * FROM schedules
-- WHERE users_id = 34 AND status = 'ACTIVE' AND lower(title) LIKE '%zzsearch%';


-- ═════════════════════════════════════════════════════════════
-- ② 복합 인덱스 (users_id, status)
-- ═════════════════════════════════════════════════════════════

-- ALTER TABLE schedules   ADD INDEX idx_schedules_users_status   (users_id, status);
-- ALTER TABLE links       ADD INDEX idx_links_users_status       (users_id, status);
-- ALTER TABLE texts       ADD INDEX idx_texts_users_status       (users_id, status);
-- ALTER TABLE folders     ADD INDEX idx_folders_users_status     (users_id, status);
-- ALTER TABLE attachments ADD INDEX idx_attachments_users_status (users_id, status, attachments_type);
-- ANALYZE TABLE folders, schedules, links, texts, attachments;


-- ═════════════════════════════════════════════════════════════
-- ③ 커버링 인덱스 — 검색 컬럼까지 인덱스에 넣는다
--    인덱스를 훑으며 LIKE 를 평가하므로 안 맞는 행은 테이블에 가지 않는다
--    (북마크 조회 1,000회 → 매칭된 10회)
--    대신 인덱스가 커진다. 그 비용도 같이 잰다.
-- ═════════════════════════════════════════════════════════════

-- ALTER TABLE schedules   DROP INDEX idx_schedules_users_status;
-- ALTER TABLE schedules   ADD INDEX idx_schedules_users_status (users_id, status, title);
--
-- ALTER TABLE links       DROP INDEX idx_links_users_status;
-- ALTER TABLE links       ADD INDEX idx_links_users_status (users_id, status, links_name);
--
-- ALTER TABLE texts       DROP INDEX idx_texts_users_status;
-- ALTER TABLE texts       ADD INDEX idx_texts_users_status (users_id, status, text_content(100));
--
-- ALTER TABLE folders     DROP INDEX idx_folders_users_status;
-- ALTER TABLE folders     ADD INDEX idx_folders_users_status (users_id, status, name);
--
-- ALTER TABLE attachments DROP INDEX idx_attachments_users_status;
-- ALTER TABLE attachments ADD INDEX idx_attachments_users_status (users_id, status, attachments_type, file_name);
-- ANALYZE TABLE folders, schedules, links, texts, attachments;


-- ═════════════════════════════════════════════════════════════
-- ④ 마지막에 FK 복구 — 0번의 restore_fk_ddl 을 붙여넣을 것
-- ═════════════════════════════════════════════════════════════


-- ═════════════════════════════════════════════════════════════
-- 인덱스 크기 — 트레이드오프 측정용
--   복합 vs 커버링 단계에서 각각 재서 비교한다
-- ═════════════════════════════════════════════════════════════
SELECT TABLE_NAME,
       ROUND(DATA_LENGTH  / 1024 / 1024, 2) AS data_mb,
       ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS index_mb,
       ROUND(INDEX_LENGTH / NULLIF(DATA_LENGTH, 0), 2) AS index_ratio
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('folders','schedules','links','texts','attachments')
ORDER BY TABLE_NAME;
