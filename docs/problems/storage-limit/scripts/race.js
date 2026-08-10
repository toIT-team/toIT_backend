// 스토리지 5GB 제한 — 동시 요청 경쟁 조건 재현 (k6)
//
// validateStorageLimit 은 "SUM 조회 → 검증 → PENDING INSERT" 구조라,
// 그 사이에 다른 요청이 끼어들면 같은 사용량을 읽고 전부 통과한다.
// 노리는 틈이 몇 ms 라 curl 을 백그라운드로 띄우는 방식으로는 재현이 어렵다.
// VU 를 미리 만들어 두고 동시에 출발시켜야 겹친다.
//
// 선행: setup.sql 로 남은 용량을 좁혀 둘 것 (기본 10MB)
// 실행: 테스트 서버 안에서 (Rate Limiter 우회를 위해 앱 포트로 직접)
//
//   TOKEN="<uid 32 토큰>" k6 run race.js
//   TOKEN="..." VUS=50 k6 run race.js
//
// 재실행 전에는 teardown.sql → setup.sql 로 상태를 되돌릴 것.

import http from 'k6/http';
import { Counter } from 'k6/metrics';

const API = __ENV.API || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN;
const FOLDER = Number(__ENV.FOLDER || 4096);
const VUS = Number(__ENV.VUS || 20);
const FILE_SIZE = Number(__ENV.FILE_SIZE || 5 * 1024 * 1024); // 5MB
// 앱은 이미지를 최대 3장까지 한 요청에 묶어 보낸다. 실제 업로드 단위에 맞춘다.
const FILES_PER_REQUEST = Number(__ENV.FILES_PER_REQUEST || 3);
const REQUEST_BYTES = FILE_SIZE * FILES_PER_REQUEST;

const passed = new Counter('limit_passed');   // 200 — 제한을 통과한 요청
const blocked = new Counter('limit_blocked'); // 400 — 용량 초과로 차단
const other = new Counter('limit_other');

export const options = {
  scenarios: {
    race: {
      // VU 를 미리 띄운 뒤 각자 1회씩만 요청 → 동시에 출발
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: 1,
      maxDuration: '60s',
    },
  },
  // 통과 건수가 많은 것이 이 테스트의 관찰 대상이므로 실패로 처리하지 않는다
  thresholds: {},
};

export function setup() {
  if (!TOKEN) {
    throw new Error('TOKEN 환경변수가 필요합니다');
  }
  console.log(
    `동시 ${VUS} 요청 | 요청당 ${FILES_PER_REQUEST}장 × ${FILE_SIZE} = ${REQUEST_BYTES} bytes` +
    ` | 총 ${VUS * REQUEST_BYTES} bytes`
  );
}

export default function () {
  const files = [];
  for (let n = 1; n <= FILES_PER_REQUEST; n++) {
    files.push({
      fileName: `race-${__VU}-${n}.jpg`,
      contentType: 'image/jpeg',
      fileSize: FILE_SIZE,
    });
  }

  const body = JSON.stringify({
    foldersIdList: [FOLDER],
    attachmentsType: 'IMAGE',
    textContent: '',
    files,
  });

  const res = http.post(`${API}/attachments/presign`, body, {
    headers: {
      Authorization: `Bearer ${TOKEN}`,
      'Content-Type': 'application/json',
    },
    timeout: '30s',
  });

  if (res.status === 200) {
    passed.add(1);
  } else if (res.status === 400) {
    blocked.add(1);
  } else {
    other.add(1);
    console.error(`VU ${__VU}: status=${res.status} body=${String(res.body).slice(0, 120)}`);
  }
}

export function teardown() {
  console.log('');
  console.log('DB 에서 최종 사용량을 확인할 것:');
  console.log("  SELECT upload_status, COUNT(*), SUM(attachments_size)");
  console.log("  FROM attachments WHERE users_id = 32 AND status = 'ACTIVE' GROUP BY upload_status;");
  console.log('');
  console.log("  SELECT SUM(attachments_size) - 5368709120 AS over_bytes");
  console.log("  FROM attachments WHERE users_id = 32 AND status = 'ACTIVE';");
}
