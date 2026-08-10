-- [1단계] 부하 테스트용 유저 30명 생성 (MySQL)
--
-- 원본: 01_create_users_dbeaver.sql (PostgreSQL)
-- 변환: DO $$ 블록 → 순수 SQL, generate_series → seq_numbers, || → CONCAT
--
-- 선행: 00_numbers_mysql.sql 먼저 실행
-- 사용법: 전체 실행.
-- 기존 유저(1·2번 등)는 절대 건드리지 않는다. 이메일 패턴 'loadtestN@toit.local' 로만 생성.
-- 멱등: 이미 있는 이메일은 건너뛴다(여러 번 돌려도 안전).

SET @v_count = 30;   -- 생성할 유저 수

-- auth_provider(NOT NULL) + provider_users_id(NOT NULL, (auth_provider, provider_users_id) UNIQUE) 채움
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

-- 생성된 유저 id 확인 (시드/토큰 발급에 사용)
SELECT users_id, email FROM users WHERE email LIKE 'loadtest%@toit.local' ORDER BY users_id;
