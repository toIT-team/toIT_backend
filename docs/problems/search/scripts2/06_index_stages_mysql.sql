-- 통합검색 인덱스 단계별 상태 만들기 (테스트 서버 전용)
--
-- 부하 테스트를 세 상태에서 돌려 비교한다.
--   ① 인덱스 없음
--   ② 복합 인덱스 (users_id, status)
--   ③ 커버링 인덱스 (users_id, status, 검색컬럼)
--
-- ⚠ InnoDB 는 FK 컬럼에 인덱스를 강제한다. (users_id, status) 의 맨 앞이 users_id 라
--   이 인덱스가 FK 인덱스를 겸하고 있어, 그냥 지우면 이 에러가 난다.
--     Cannot drop index '...': needed in a foreign key constraint
--   그래서 ① 을 만들려면 users_id FK 를 먼저 떼야 한다.
--
-- ⚠ spring.jpa.hibernate.ddl-auto=update 이므로 측정 중에는 앱을 재시작하지 말 것.
--   재시작하면 JPA 가 @Index 와 FK 를 다시 만든다.
--
-- ⚠ schedules 에는 FK 가 두 개다. folders_id 쪽은 인덱스를 막지 않으므로 건드리지 않는다.
--
-- DBeaver 에서는 블록을 선택하고 Alt+X (Execute script).

-- ═════════════════════════════════════════════════════════════
-- STEP 1. users_id FK 제거
-- ═════════════════════════════════════════════════════════════

ALTER TABLE attachments DROP FOREIGN KEY FKi22hi6wi6bdni1tx1sqorj8ik;
ALTER TABLE folders     DROP FOREIGN KEY FK26jum51lh2evti3dtqolijqx3;
ALTER TABLE links       DROP FOREIGN KEY FK242hmcmqi297vik23okxya6je;
ALTER TABLE schedules   DROP FOREIGN KEY FKdb7rash9ovdgr553udi1f7yb9;
ALTER TABLE texts       DROP FOREIGN KEY FKc3tt5gevawaicppn67in1hpj3;


-- ═════════════════════════════════════════════════════════════
-- STEP 2. 인덱스 제거 → ① 인덱스 없는 상태
-- ═════════════════════════════════════════════════════════════

ALTER TABLE schedules   DROP INDEX idx_schedules_users_status;
ALTER TABLE links       DROP INDEX idx_links_users_status;
ALTER TABLE texts       DROP INDEX idx_texts_users_status;
ALTER TABLE folders     DROP INDEX idx_folders_users_status;
ALTER TABLE attachments DROP INDEX idx_attachments_users_status;

ANALYZE TABLE folders, schedules, links, texts, attachments;

-- 확인 — remaining 0, type=ALL / key=NULL 이어야 한다
SELECT COUNT(*) AS remaining FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%_users_status%';

EXPLAIN SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE' AND title LIKE '%zzsearch%';

-- 인덱스 크기 (트레이드오프 비교용 — 이 상태의 값을 적어둘 것)
SELECT TABLE_NAME,
       ROUND(DATA_LENGTH  / 1024 / 1024, 2) AS data_mb,
       ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS index_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('folders','schedules','links','texts','attachments')
ORDER BY TABLE_NAME;

-- ▶ 여기서 부하 테스트 실행 (① baseline)


-- ═════════════════════════════════════════════════════════════
-- STEP 3. 복합 인덱스 → ② 상태
-- ═════════════════════════════════════════════════════════════

ALTER TABLE schedules   ADD INDEX idx_schedules_users_status   (users_id, status);
ALTER TABLE links       ADD INDEX idx_links_users_status       (users_id, status);
ALTER TABLE texts       ADD INDEX idx_texts_users_status       (users_id, status);
ALTER TABLE folders     ADD INDEX idx_folders_users_status     (users_id, status);
ALTER TABLE attachments ADD INDEX idx_attachments_users_status (users_id, status, attachments_type);

ANALYZE TABLE folders, schedules, links, texts, attachments;

EXPLAIN SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE' AND title LIKE '%zzsearch%';

SELECT TABLE_NAME,
       ROUND(DATA_LENGTH  / 1024 / 1024, 2) AS data_mb,
       ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS index_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('folders','schedules','links','texts','attachments')
ORDER BY TABLE_NAME;

-- ▶ 여기서 부하 테스트 실행 (② 복합 인덱스)


-- ═════════════════════════════════════════════════════════════
-- STEP 4. 커버링 인덱스 → ③ 상태
--   검색 컬럼을 인덱스에 넣으면 인덱스를 훑으며 LIKE 를 평가할 수 있어,
--   안 맞는 행은 테이블에 가지 않는다 (북마크 조회 1,000회 → 10회)
--   대신 인덱스가 커진다. index_mb 를 같이 본다.
-- ═════════════════════════════════════════════════════════════

ALTER TABLE schedules   DROP INDEX idx_schedules_users_status;
ALTER TABLE schedules   ADD  INDEX idx_schedules_users_status (users_id, status, title);

ALTER TABLE links       DROP INDEX idx_links_users_status;
ALTER TABLE links       ADD  INDEX idx_links_users_status (users_id, status, links_name);

-- text_content 는 길 수 있어 접두 100자만 인덱싱한다
ALTER TABLE texts       DROP INDEX idx_texts_users_status;
ALTER TABLE texts       ADD  INDEX idx_texts_users_status (users_id, status, text_content(100));

ALTER TABLE folders     DROP INDEX idx_folders_users_status;
ALTER TABLE folders     ADD  INDEX idx_folders_users_status (users_id, status, name);

ALTER TABLE attachments DROP INDEX idx_attachments_users_status;
ALTER TABLE attachments ADD  INDEX idx_attachments_users_status (users_id, status, attachments_type, file_name);

ANALYZE TABLE folders, schedules, links, texts, attachments;

EXPLAIN SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE' AND title LIKE '%zzsearch%';

EXPLAIN ANALYZE SELECT * FROM schedules
WHERE users_id = 100 AND status = 'ACTIVE' AND title LIKE '%zzsearch%';

SELECT TABLE_NAME,
       ROUND(DATA_LENGTH  / 1024 / 1024, 2) AS data_mb,
       ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS index_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('folders','schedules','links','texts','attachments')
ORDER BY TABLE_NAME;

-- ▶ 여기서 부하 테스트 실행 (③ 커버링 인덱스)


-- ═════════════════════════════════════════════════════════════
-- STEP 5. 원상복구 — 인덱스를 엔티티 정의대로 되돌리고 FK 복구
-- ═════════════════════════════════════════════════════════════

ALTER TABLE schedules   DROP INDEX idx_schedules_users_status;
ALTER TABLE schedules   ADD  INDEX idx_schedules_users_status (users_id, status);
ALTER TABLE links       DROP INDEX idx_links_users_status;
ALTER TABLE links       ADD  INDEX idx_links_users_status (users_id, status);
ALTER TABLE texts       DROP INDEX idx_texts_users_status;
ALTER TABLE texts       ADD  INDEX idx_texts_users_status (users_id, status);
ALTER TABLE folders     DROP INDEX idx_folders_users_status;
ALTER TABLE folders     ADD  INDEX idx_folders_users_status (users_id, status);
ALTER TABLE attachments DROP INDEX idx_attachments_users_status;
ALTER TABLE attachments ADD  INDEX idx_attachments_users_status (users_id, status, attachments_type);

ALTER TABLE attachments ADD CONSTRAINT FKi22hi6wi6bdni1tx1sqorj8ik FOREIGN KEY (users_id) REFERENCES users (users_id);
ALTER TABLE folders     ADD CONSTRAINT FK26jum51lh2evti3dtqolijqx3 FOREIGN KEY (users_id) REFERENCES users (users_id);
ALTER TABLE links       ADD CONSTRAINT FK242hmcmqi297vik23okxya6je FOREIGN KEY (users_id) REFERENCES users (users_id);
ALTER TABLE schedules   ADD CONSTRAINT FKdb7rash9ovdgr553udi1f7yb9 FOREIGN KEY (users_id) REFERENCES users (users_id);
ALTER TABLE texts       ADD CONSTRAINT FKc3tt5gevawaicppn67in1hpj3 FOREIGN KEY (users_id) REFERENCES users (users_id);

ANALYZE TABLE folders, schedules, links, texts, attachments;

-- 확인 — 인덱스 11컬럼, FK 6개
SELECT COUNT(*) AS index_columns FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%_users_status%';

SELECT COUNT(*) AS fk_count FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('folders','schedules','links','texts','attachments')
  AND REFERENCED_TABLE_NAME IS NOT NULL;
