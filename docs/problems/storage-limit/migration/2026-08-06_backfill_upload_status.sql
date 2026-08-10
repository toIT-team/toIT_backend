-- attachments.upload_status 백필 (MySQL)
--
-- 배경:
--   업로드 예약 구조(PENDING) 도입으로 attachments 에 upload_status 컬럼이 추가된다.
--   ddl-auto=update 는 컬럼을 추가해 주지만 기존 행은 NULL 로 남는다.
--   기존 행은 모두 이미 확정된 첨부이므로 CONFIRMED 로 채운다.
--
-- 실행 시점:
--   애플리케이션을 새 버전으로 한 번 기동해 컬럼이 생긴 직후, 서비스 재개 전.
--   컬럼이 nullable=false 이지만 ddl-auto=update 는 기존 행 때문에 NULL 을 허용한 상태로 만든다.
--
-- 실행:
--   docker exec -i -e MYSQL_PWD=<root비밀번호> <컨테이너> \
--     mysql -u root --default-character-set=utf8mb4 <DB> < 2026-08-06_backfill_upload_status.sql

-- 백필 전 확인: NULL 인 행 수
SELECT COUNT(*) AS null_before FROM attachments WHERE upload_status IS NULL;

UPDATE attachments
SET upload_status = 'CONFIRMED'
WHERE upload_status IS NULL;

-- 백필 후 확인: null_after 는 0, confirmed 는 기존 전체 건수와 같아야 한다
SELECT
    SUM(upload_status IS NULL)                AS null_after,
    SUM(upload_status = 'CONFIRMED')          AS confirmed,
    SUM(upload_status = 'PENDING')            AS pending,
    COUNT(*)                                  AS total
FROM attachments;
