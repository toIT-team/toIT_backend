-- [준비] 스토리지 용량 경쟁 조건 재현용 — 대상 유저의 남은 용량을 좁힌다 (MySQL)
--
-- 목적:
--   5GB 제한에 걸리는 상황을 만들어야 presign 반복 차단 / 동시 요청 초과를 측정할 수 있다.
--   실제 S3 객체는 필요 없다. validateStorageLimit 은 DB 의 SUM 만 보기 때문.
--
-- 주의:
--   1) 테스트 서버 전용. 운영에서 절대 실행하지 말 것.
--   2) upload_status 는 CONFIRMED 로 넣는다. PENDING 으로 넣으면 정리 배치가
--      10분 뒤 회수해 테스트 도중 용량이 되돌아간다.
--   3) 사용자 변수(@v_...)를 쓰지 않는다. DBeaver 등에서 문 사이에 값이 유지되지
--      않아 NULL 로 새는 경우가 있다. 아래 값은 직접 수정할 것.
--
-- ┌─ 수정할 값 ────────────────────────────────────────────────┐
-- │ users_id       32                                          │
-- │ folders_id     4096   (해당 유저 소유여야 함)               │
-- │ 남길 여유      10485760  (10MB)                             │
-- │ 한도           5368709120 (5GB, StorageUsageResponse 와 동일)│
-- └────────────────────────────────────────────────────────────┘
--
-- 정리: capacity_race_teardown_mysql.sql

-- 1) 현재 사용량 확인
SELECT
    COALESCE(SUM(attachments_size), 0)                                       AS used_before,
    5368709120 - COALESCE(SUM(attachments_size), 0)                          AS remaining_before,
    ROUND((5368709120 - COALESCE(SUM(attachments_size), 0)) / 1048576, 2)    AS remaining_mb_before
FROM attachments
WHERE users_id = 32 AND status = 'ACTIVE';

-- 2) 남은 용량이 10MB 가 되도록 filler 한 행 삽입
--    파생 테이블로 감싸 대상 테이블 자기참조 문제를 피한다.
INSERT INTO attachments (
    created_at, status, upload_status, storage_id, text_content,
    attachments_extension, attachments_size, attachments_type, file_name,
    object_key, presigned_url, users_id
)
SELECT
    NOW(6), 'ACTIVE', 'CONFIRMED', 4096, NULL,
    'application/octet-stream', u.filler, 'FILE', 'capacity-filler.bin',
    'racetest/filler/32', '', 32
FROM (
    SELECT 5368709120 - 10485760 - COALESCE(SUM(attachments_size), 0) AS filler
    FROM attachments
    WHERE users_id = 32 AND status = 'ACTIVE'
) u
WHERE u.filler > 0;

-- 3) 결과 확인: remaining_mb 가 10.00 이어야 한다
SELECT
    COALESCE(SUM(attachments_size), 0)                                    AS used_after,
    5368709120 - COALESCE(SUM(attachments_size), 0)                       AS remaining_after,
    ROUND((5368709120 - COALESCE(SUM(attachments_size), 0)) / 1048576, 2) AS remaining_mb
FROM attachments
WHERE users_id = 32 AND status = 'ACTIVE';
