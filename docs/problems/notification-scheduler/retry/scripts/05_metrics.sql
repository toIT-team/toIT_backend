-- QPS · TPS 측정용. 발송 직전에 한 번, 발송이 끝난 뒤에 한 번 실행한다.
--
--   1회차   스냅샷만 찍힌다
--   2회차   스냅샷을 찍고 1회차와의 차이를 보여준다
--
-- MySQL 은 켜진 뒤로 "쿼리 몇 개 받았나", "행 몇 개 읽었나" 를 계속 세고 있다.
-- 그 누적값을 두 번 읽어 빼는 것이 전부다.
--
-- Com_commit 같은 Com_* 은 SHOW GLOBAL STATUS 로만 나오고
-- performance_schema.global_status 에는 없어서 Handler_* 로 대신한다.
--   Handler_commit   문장 단위 커밋까지 세므로 Com_commit 보다 크게 나온다.
--                    절대값보다 "전후 차이" 를 보는 용도다.
--
-- 주의 : 전역 카운터라 다른 사람이 앱을 쓰면 그 요청도 섞인다.
--        측정 중에는 아무도 안 쓰게 한다.

CREATE TABLE IF NOT EXISTS metrics_snapshot (
    id                     INT AUTO_INCREMENT PRIMARY KEY,
    taken_at               DATETIME(3) NOT NULL,
    questions              BIGINT,
    handler_commit         BIGINT,
    handler_rollback       BIGINT,
    handler_write          BIGINT,
    handler_update         BIGINT,
    innodb_rows_read       BIGINT,
    innodb_rows_inserted   BIGINT,
    innodb_rows_updated    BIGINT
);

INSERT INTO metrics_snapshot (
    taken_at, questions, handler_commit, handler_rollback,
    handler_write, handler_update,
    innodb_rows_read, innodb_rows_inserted, innodb_rows_updated
)
SELECT
    NOW(3),
    MAX(IF(VARIABLE_NAME = 'Questions',            VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Handler_commit',       VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Handler_rollback',     VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Handler_write',        VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Handler_update',       VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Innodb_rows_read',     VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Innodb_rows_inserted', VARIABLE_VALUE, NULL)),
    MAX(IF(VARIABLE_NAME = 'Innodb_rows_updated',  VARIABLE_VALUE, NULL))
FROM performance_schema.global_status
WHERE VARIABLE_NAME IN (
    'Questions', 'Handler_commit', 'Handler_rollback',
    'Handler_write', 'Handler_update',
    'Innodb_rows_read', 'Innodb_rows_inserted', 'Innodb_rows_updated'
);

-- 지금 몇 개가 찍혔는지
SELECT
    COUNT(*) AS 찍힌_스냅샷,
    IF(COUNT(*) < 2,
       '아직 하나뿐이다. 발송이 끝난 뒤 이 파일을 한 번 더 실행하면 차이가 나온다.',
       '아래에 직전 스냅샷과의 차이가 나온다.') AS 안내
FROM metrics_snapshot;

-- 직전 스냅샷과의 차이
SELECT
    ROUND(TIMESTAMPDIFF(MICROSECOND, prev.taken_at, cur.taken_at) / 1e6, 1) AS 경과_초,

    cur.questions      - prev.questions                                     AS 쿼리_수,
    ROUND((cur.questions - prev.questions)
          / (TIMESTAMPDIFF(MICROSECOND, prev.taken_at, cur.taken_at) / 1e6), 1) AS QPS,

    cur.handler_commit - prev.handler_commit                                AS 커밋_수,
    ROUND((cur.handler_commit - prev.handler_commit)
          / (TIMESTAMPDIFF(MICROSECOND, prev.taken_at, cur.taken_at) / 1e6), 1) AS TPS,

    cur.handler_rollback - prev.handler_rollback                            AS 롤백_수
FROM metrics_snapshot cur
JOIN metrics_snapshot prev
  ON prev.id = (SELECT MAX(id) FROM metrics_snapshot WHERE id < cur.id)
ORDER BY cur.id DESC
LIMIT 1;

-- 알림 1건당 몇 개씩 썼는지
--   건수는 유실테스트 데이터에서 세므로 @COUNT 를 바꿔도 알아서 맞는다
--   쿼리   예상 3~5개.  조회 · 알림함 INSERT · 상태 UPDATE · 토큰 조회 · 설정 조회
--   읽은행 이 값이 크면 인덱스를 못 타거나 전체 스캔이 있다는 뜻이다
SET @N = (SELECT COUNT(*) FROM schedules_alarm a
          JOIN schedules s ON s.schedules_id = a.schedules_id
          WHERE s.title LIKE '유실테스트-%');

SELECT
    @N                                                                     AS 알림_건수,
    ROUND((cur.questions            - prev.questions)            / @N, 1)  AS 건당_쿼리,
    ROUND((cur.innodb_rows_read     - prev.innodb_rows_read)     / @N, 1)  AS 건당_읽은행,
    cur.innodb_rows_inserted - prev.innodb_rows_inserted                   AS 넣은_행,
    cur.innodb_rows_updated  - prev.innodb_rows_updated                    AS 고친_행,
    cur.handler_write        - prev.handler_write                          AS INSERT_호출,
    cur.handler_update       - prev.handler_update                         AS UPDATE_호출
FROM metrics_snapshot cur
JOIN metrics_snapshot prev
  ON prev.id = (SELECT MAX(id) FROM metrics_snapshot WHERE id < cur.id)
ORDER BY cur.id DESC
LIMIT 1;
