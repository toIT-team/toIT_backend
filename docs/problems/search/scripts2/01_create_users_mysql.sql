-- [1단계] 부하 테스트용 유저 생성 (MySQL)
--
-- 실제 서비스에서는 가입자 전원이 동시에 들어오지 않는다.
-- 가입자 200명을 만들어 두고 부하 테스트는 그중 일부(VU 수)만 활동시킨다.
--
-- 선행: 00_numbers_mysql.sql
-- 사용법: 전체 실행 (DBeaver 는 Alt+X)
-- 멱등: 이미 있는 이메일은 건너뛴다. 여러 번 돌려도 안전하다.
--
-- 기존 유저(1·2번 등)는 건드리지 않는다. 이메일 패턴 'loadtestN@toit.local' 로만 만든다.

SET @v_count = 200;

INSERT INTO users (email, name, role, status, auth_provider, provider_users_id, created_at)
SELECT CONCAT('loadtest', s.n, '@toit.local'),
       CONCAT('loadtest ', s.n),
       'ROLE_USER',
       'ACTIVE',
       'KAKAO',
       CONCAT('loadtest-', s.n),
       NOW()
FROM seq_numbers s
WHERE s.n <= @v_count
  AND NOT EXISTS (
      SELECT 1 FROM users u WHERE u.email = CONCAT('loadtest', s.n, '@toit.local')
  );

-- 생성 결과. 토큰 발급 시 이 id 범위를 gen_tokens.py 에 넣는다.
SELECT COUNT(*) AS created,
       MIN(users_id) AS uid_start,
       MAX(users_id) AS uid_end
FROM users WHERE email LIKE 'loadtest%@toit.local';
