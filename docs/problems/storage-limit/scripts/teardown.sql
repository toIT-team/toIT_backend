-- [정리] 용량 경쟁 조건 재현용 데이터 제거 (MySQL)
--
-- capacity_race_setup_mysql.sql 로 넣은 filler 행과,
-- 테스트 중 만들어진 예약(PENDING) 행을 지운다.
--
-- 주의: 테스트 서버 전용. users_id 는 직접 수정할 것 (기본 32).

-- 1) 정리 전 현황
SELECT
    SUM(file_name = 'capacity-filler.bin')  AS filler_rows,
    SUM(upload_status = 'PENDING')          AS pending_rows,
    COALESCE(SUM(attachments_size), 0)      AS used_before
FROM attachments
WHERE users_id = 32 AND status = 'ACTIVE';

-- 2) filler 제거
DELETE FROM attachments
WHERE users_id = 32 AND file_name = 'capacity-filler.bin';

-- 3) 테스트로 생긴 예약 제거 (정리 배치를 기다리지 않고 즉시)
DELETE FROM attachments
WHERE users_id = 32 AND upload_status = 'PENDING';

-- 4) 정리 후: 테스트 시작 전 사용량으로 돌아와야 한다
SELECT
    COALESCE(SUM(attachments_size), 0)                                    AS used_after,
    COUNT(*)                                                              AS rows_after,
    ROUND((5368709120 - COALESCE(SUM(attachments_size), 0)) / 1048576, 2) AS remaining_mb
FROM attachments
WHERE users_id = 32 AND status = 'ACTIVE';
