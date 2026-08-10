#!/bin/bash
#
# MySQL 운영 DB 백업 → S3 업로드
#
# 원본: backup_postgres_to_s3.sh (2026-08-06 MySQL 전환으로 교체)
# 배포: scp 로 서버 ~/backup_mysql_to_s3.sh 에 두고 crontab 등록
#   0 3 * * * /home/ubuntu/backup_mysql_to_s3.sh >> /home/ubuntu/db_backup.log 2>&1
#
# 비밀번호는 .env.prod 에서 읽는다(스크립트에 하드코딩하지 않음).
# 명령행 인자 대신 MYSQL_PWD 환경변수로 넘겨 `ps` 노출과 경고를 피한다.

set -euo pipefail

# 기본값은 운영. 테스트 서버에서는 환경변수로 덮어쓴다.
#   BACKUP_CONTAINER=toit-test-mysql BACKUP_DB=toit_test \
#   BACKUP_ENV_FILE=/home/ubuntu/toit-deploy/.env.test \
#   BACKUP_S3=s3://.../test/mysql ./backup_mysql_to_s3.sh
CONTAINER_NAME="${BACKUP_CONTAINER:-toit-prod-mysql}"
DB_NAME="${BACKUP_DB:-toit}"
DB_USER="root"
ENV_FILE="${BACKUP_ENV_FILE:-/home/ubuntu/toit-deploy/.env.prod}"

LOCAL_BACKUP_DIR="${BACKUP_DIR:-/home/ubuntu/db_backups}"
S3_BUCKET="${BACKUP_S3:-s3://toit-db-backup-711387097345-ap-northeast-2-an/main/mysql}"

DATE=$(date +"%Y-%m-%d_%H-%M-%S")
FILE_NAME="${DB_NAME}_${DATE}.sql.gz"

TMP_BACKUP_FILE="/tmp/${FILE_NAME}"
LOCAL_BACKUP_FILE="${LOCAL_BACKUP_DIR}/${FILE_NAME}"

echo "[$(date)] MySQL Docker backup start"

# .env.prod 는 DB_URL 에 & 가 있어 source 하면 깨진다. 필요한 줄만 뽑는다.
DB_PASSWORD=$(grep '^MYSQL_ROOT_PASSWORD=' "$ENV_FILE" | cut -d= -f2-)
if [ -z "$DB_PASSWORD" ]; then
    echo "[$(date)] ERROR: MYSQL_ROOT_PASSWORD 를 $ENV_FILE 에서 찾지 못했습니다"
    exit 1
fi

mkdir -p "$LOCAL_BACKUP_DIR"
rm -f "$TMP_BACKUP_FILE"

echo "[$(date)] Dump from Docker container: $CONTAINER_NAME"

# --single-transaction : InnoDB 일관 스냅샷. 없으면 테이블 락으로 서비스가 멈춘다
# --default-character-set=utf8mb4 : 한글·이모지 보존
# --no-tablespaces : PROCESS 권한 요구 회피
# --set-gtid-purged=OFF : 복원 시 GTID 충돌 방지
docker exec -i -e MYSQL_PWD="$DB_PASSWORD" "$CONTAINER_NAME" \
    mysqldump -u "$DB_USER" \
    --single-transaction \
    --routines --triggers --events \
    --default-character-set=utf8mb4 \
    --no-tablespaces \
    --set-gtid-purged=OFF \
    "$DB_NAME" | gzip > "$TMP_BACKUP_FILE"

# 빈 파일·잘린 덤프를 그대로 올리면 "백업이 도는 것처럼 보이는" 최악의 실패가 된다
if [ ! -s "$TMP_BACKUP_FILE" ]; then
    echo "[$(date)] ERROR: 덤프 파일이 비어 있습니다"
    rm -f "$TMP_BACKUP_FILE"
    exit 1
fi

# mysqldump 는 정상 종료 시 마지막에 "Dump completed on" 을 남긴다
if ! gzip -dc "$TMP_BACKUP_FILE" | tail -5 | grep -q "Dump completed"; then
    echo "[$(date)] ERROR: 덤프가 완결되지 않았습니다 (Dump completed 없음)"
    rm -f "$TMP_BACKUP_FILE"
    exit 1
fi

TABLE_COUNT=$(gzip -dc "$TMP_BACKUP_FILE" | grep -c "^CREATE TABLE" || true)
echo "[$(date)] Dump OK: $(du -h "$TMP_BACKUP_FILE" | cut -f1), tables=${TABLE_COUNT}"

mv "$TMP_BACKUP_FILE" "$LOCAL_BACKUP_FILE"

echo "[$(date)] Upload to S3: $LOCAL_BACKUP_FILE"
aws s3 cp "$LOCAL_BACKUP_FILE" "$S3_BUCKET/"

echo "[$(date)] Remove local backups older than 7 days"
find "$LOCAL_BACKUP_DIR" -type f -name "*.sql.gz" -mtime +7 -delete

echo "[$(date)] Backup completed"
