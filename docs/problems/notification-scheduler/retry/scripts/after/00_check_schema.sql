-- 시작하기 전에 컬럼 이름이 아래 스크립트와 맞는지 확인한다.
-- 다르면 01~04 의 컬럼명을 고쳐야 한다.

SHOW CREATE TABLE schedules\G
SHOW CREATE TABLE schedules_alarm\G

-- 기대하는 컬럼
--
-- schedules
--   schedules_id  title  app_color  folders_id  time_setting
--   start_date  end_date  start_time  end_time
--   status  memo  created_at  deleted_at  users_id
--
-- schedules_alarm
--   schedules_alarm_id  schedules_id  alarm_state
--   alarm_date_time  alarm_offset_minutes  is_read
--   status  last_error_code  attempt_count  next_attempt_at   (07_migrate.sql 이후)

-- 내 users_id 확인
SELECT users_id, name FROM users ORDER BY users_id DESC LIMIT 5;
