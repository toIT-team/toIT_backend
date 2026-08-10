#!/bin/bash
#
# 스토리지 5GB 제한 — 동시 요청 경쟁 조건 재현
#
# 같은 사용자로 presign 을 동시에 N 건 보낸다.
# validateStorageLimit 이 "SUM 조회 → 검증 → PENDING INSERT" 구조라,
# 동시에 들어온 요청들이 서로의 예약을 보지 못하고 전부 통과할 수 있다.
#
# 실행: 테스트 서버 안에서 (NGINX Rate Limiter 를 우회하려면 앱 포트로 직접)
#   ./race_concurrent.sh
#
# 선행: setup.sql 로 남은 용량을 좁혀 둘 것.
#       재실행 전에는 teardown.sql → setup.sql 로 상태를 되돌릴 것.

set -u

API="${API:-http://localhost:8080}"
TOKEN="${TOKEN:?TOKEN 환경변수가 필요합니다}"
FOLDER="${FOLDER:-4096}"
CONCURRENCY="${CONCURRENCY:-20}"
FILE_SIZE="${FILE_SIZE:-5242880}"   # 5MB

OUT_DIR=$(mktemp -d)
trap 'rm -rf "$OUT_DIR"' EXIT

echo "동시 요청 ${CONCURRENCY}건 | 건당 ${FILE_SIZE} bytes | 총 요청량 $((CONCURRENCY * FILE_SIZE)) bytes"
echo "API=${API} FOLDER=${FOLDER}"
echo

# 모든 curl 을 백그라운드로 띄워 최대한 겹치게 한다.
for i in $(seq 1 "$CONCURRENCY"); do
    curl -s --max-time 30 \
        -o "${OUT_DIR}/body_${i}" \
        -w "%{http_code}" \
        -X POST "${API}/attachments/presign" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{\"foldersIdList\":[${FOLDER}],\"attachmentsType\":\"IMAGE\",\"textContent\":\"\",\"files\":[{\"fileName\":\"race-${i}.jpg\",\"contentType\":\"image/jpeg\",\"fileSize\":${FILE_SIZE}}]}" \
        > "${OUT_DIR}/code_${i}" &
done
wait

OK=0
BLOCKED=0
OTHER=0
for i in $(seq 1 "$CONCURRENCY"); do
    code=$(cat "${OUT_DIR}/code_${i}" 2>/dev/null)
    case "$code" in
        200) OK=$((OK + 1)) ;;
        400) BLOCKED=$((BLOCKED + 1)) ;;
        *)   OTHER=$((OTHER + 1))
             echo "  [기타 응답] code=${code} body=$(head -c 120 "${OUT_DIR}/body_${i}")" ;;
    esac
done

echo "결과"
echo "  통과(200)      ${OK}"
echo "  차단(400)      ${BLOCKED}"
echo "  기타           ${OTHER}"
echo "  통과분 합계    $((OK * FILE_SIZE)) bytes"
echo
echo "DB 에서 최종 사용량을 확인할 것:"
cat <<'SQL'
  SELECT upload_status, COUNT(*) AS rows, SUM(attachments_size) AS bytes
  FROM attachments WHERE users_id = 32 AND status = 'ACTIVE'
  GROUP BY upload_status;

  SELECT SUM(attachments_size)                      AS used,
         SUM(attachments_size) - 5368709120         AS over_bytes,
         ROUND((SUM(attachments_size) - 5368709120) / 1048576, 2) AS over_mb
  FROM attachments WHERE users_id = 32 AND status = 'ACTIVE';
SQL
