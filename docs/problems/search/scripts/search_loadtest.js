// /page/search 부하 테스트 (k6)
//
// 준비: gen_tokens.py 로 tokens.json(토큰 30개) 생성 후 같은 폴더에서 실행
// 실행: k6 run -e BASE_URL=http://<test-host>:8080 docs/problems/search/scripts/search_loadtest.js
//   옵션: -e KEYWORD=loadtest  (매칭 행 전부 = 최악 부하 / 다른 단어로 선택도 조절)
//
// VU(동시접속)를 10→200 으로 램프하며 30개 토큰에 분산시킨다.
// (DB 유저 30명, VU는 그 토큰들을 돌려쓰므로 VU>30 도 정상)

import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

const TOKENS = new SharedArray('tokens', () => JSON.parse(open('./tokens.json')));
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const KEYWORD = __ENV.KEYWORD || 'loadtest';

export const options = {
  stages: [
    { duration: '1m', target: 10 },   // sanity
    { duration: '2m', target: 50 },
    { duration: '2m', target: 100 },  // 목표 동시 100명
    { duration: '2m', target: 150 },
    { duration: '2m', target: 200 },  // 한계 탐색
    { duration: '1m', target: 0 },    // ramp-down
  ],
  thresholds: {
    // 클라이언트 측 빠른 확인용 (서버측 SLI 는 그라파나 le=0.7 로 별도 관찰)
    http_req_duration: ['p(95)<700'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  // VU 번호로 토큰을 분산 (같은 토큰에만 몰리는 캐시 편향 방지)
  const token = TOKENS[(__VU - 1) % TOKENS.length];
  const res = http.get(`${BASE}/page/search?keyword=${KEYWORD}`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { name: 'page_search' },
  });
  check(res, { 'status is 200': (r) => r.status === 200 });
}
