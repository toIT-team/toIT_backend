// [용량 측정] /page/search 동시성 한계(knee) 찾기 (k6)
//
// 현실적인 검색(keyword=realsearch, 유저당 50건 반환)으로 VU를 잘게 올리며
// p95 / SLI 가 700ms(=SLO)를 넘기 시작하는 동시 사용자 수 = "용량"을 찾는다.
//
// 준비: tag_realsearch_dbeaver.sql 로 realsearch 토큰 심기 + tokens.json
// 실행: k6 run -e BASE_URL=http://localhost:8080 docs/problems/search/scripts/capacity_test.js
//   비교용 worst-case: -e KEYWORD=loadtest 로 같은 램프 한 번 더
//
// knee 읽는 법: 각 stage = 1분 = VU 한 단계. 그라파나 SLI 패널(아래 쿼리)을
//   시간축으로 보며 "처음 SLI가 뚝 떨어지는(=p95>700ms) 단계의 VU"가 용량.

import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

const TOKENS = new SharedArray('tokens', () => JSON.parse(open('./tokens.json')));
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const KEYWORD = __ENV.KEYWORD || 'realsearch';

export const options = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '1m', target: 25 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 75 },
    { duration: '1m', target: 100 },
    { duration: '1m', target: 125 },
    { duration: '1m', target: 150 },
    { duration: '1m', target: 175 },
    { duration: '1m', target: 200 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    // 전체 통과/실패 판정용(참고). 단계별 knee 는 그라파나 시간축으로 확인.
    http_req_duration: ['p(95)<700'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const token = TOKENS[(__VU - 1) % TOKENS.length]; // VU를 30개 토큰에 분산
  const res = http.get(`${BASE}/page/search?keyword=${KEYWORD}`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { name: 'page_search' },
  });
  check(res, { 'status is 200': (r) => r.status === 200 });
}
