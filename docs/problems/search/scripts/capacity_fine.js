// [용량 정밀 측정] 10~30 VU 구간의 knee 찾기 (k6)
//
// 측정 2에서 "10명=통과, 30명=붕괴"까지만 알아냈으므로, 그 사이(10·15·20·25·30)를
// 촘촘히 재서 SLO(700ms)가 정확히 어디서 깨지는지 찾는다.
//
// 각 레벨을 "10초 램프 + 50초 유지(hold)"로 만들어, VU가 일정한 구간에서 SLI를 깨끗하게 읽는다.
// 준비: realsearch 태깅 + tokens.json (둘 다 서버에 이미 있음)
// 실행: k6 run -e BASE_URL=http://localhost:8080 capacity_fine.js

import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

const TOKENS = new SharedArray('tokens', () => JSON.parse(open('./tokens.json')));
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const KEYWORD = __ENV.KEYWORD || 'realsearch';

export const options = {
  stages: [
    { duration: '20s', target: 10 }, { duration: '40s', target: 10 }, // 1분: VU 10
    { duration: '10s', target: 15 }, { duration: '50s', target: 15 }, // 2분: VU 15
    { duration: '10s', target: 20 }, { duration: '50s', target: 20 }, // 3분: VU 20
    { duration: '10s', target: 25 }, { duration: '50s', target: 25 }, // 4분: VU 25
    { duration: '10s', target: 30 }, { duration: '50s', target: 30 }, // 5분: VU 30
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<700'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const token = TOKENS[(__VU - 1) % TOKENS.length];
  const res = http.get(`${BASE}/page/search?keyword=${KEYWORD}`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { name: 'page_search' },
  });
  check(res, { 'status is 200': (r) => r.status === 200 });
}
