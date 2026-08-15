// 통합검색 부하 테스트 — 동시 사용자를 단계별로 올리며 SLI 곡선 측정
//
// 단계마다 별도 시나리오를 두고 태그를 붙여, 마지막에 단계별 표로 출력한다.
// k6 한 번 실행으로 끝난다.
//
// 실행:
//   k6 run -e BASE_URL=http://localhost:8080 search_curve.js
//   k6 run -e LEVELS=5,10,15,20 -e STEP=60 search_curve.js
//
// 옵션 (전부 -e 로 전달)
//   BASE_URL   대상 서버            기본 http://localhost:8080
//   KEYWORD    검색어               기본 zzsearch
//   LEVELS     동시 사용자 단계      기본 5,10,15,20,25,30
//   STEP       단계당 지속 시간(초)   기본 30
//   WARMUP     워밍업 시간(초)       기본 30  (JIT 워밍업 전에는 값이 2~3배로 나온다)
//   WARMUP_VUS 워밍업 동시 사용자     기본 10  (측정에서 제외된다)
//   USERS      대상 사용자군         기본 avg
//                avg   평균 유저만 (앞 HEAVY_N 명 제외)  ← 일반 부하
//                heavy 헤비 유저만                      ← 한계 확인용
//                all   전체
//   HEAVY_N    헤비 유저 수          기본 5  (tokens.json 앞쪽 N 명)
//
// 준비: 같은 폴더에 tokens.json (loadtest 유저들의 JWT 배열)
//       gen_tokens.py 의 UID_START/UID_END 를 현재 유저 id 범위로 맞출 것
//
// 주의: NGINX Rate Limiter 를 우회해 앱 포트로 직접 보낸다.
//       limit_req 가 요청을 잘라내면 애플리케이션 레벨 측정이 안 된다.

import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { SharedArray } from 'k6/data';

const TOKENS  = new SharedArray('tokens', () => JSON.parse(open('./tokens.json')));
const BASE    = __ENV.BASE_URL || 'http://localhost:8080';
const KEYWORD = __ENV.KEYWORD  || 'zzsearch';
const STEP    = parseInt(__ENV.STEP   || '30', 10);
const WARMUP  = parseInt(__ENV.WARMUP || '30', 10);
const WARMUP_VUS = parseInt(__ENV.WARMUP_VUS || '10', 10);
const LEVELS  = (__ENV.LEVELS || '5,10,15,20,25,30').split(',').map((v) => parseInt(v, 10));
const USERS   = __ENV.USERS || 'avg';
const HEAVY_N = parseInt(__ENV.HEAVY_N || '5', 10);

// 토큰 앞쪽 HEAVY_N 개가 헤비 유저다. 그대로 두면 낮은 동시 구간에서
// 헤비 유저만 측정하게 되므로 대상을 골라 쓴다.
const POOL =
  USERS === 'heavy' ? TOKENS.slice(0, HEAVY_N) :
  USERS === 'all'   ? TOKENS :
                      TOKENS.slice(HEAVY_N);

// SLI — 700ms 이내에 응답한 요청의 비율
export const sli = new Rate('sli_700ms');

// 단계마다 시나리오를 하나씩 만들고 startTime 으로 순차 실행시킨다.
const scenarios = {};
const thresholds = {};

if (WARMUP > 0) {
  scenarios.warmup = {
    executor: 'constant-vus',
    vus: WARMUP_VUS,
    duration: `${WARMUP}s`,
    startTime: '0s',
    tags: { level: 'warmup' },
    exec: 'search',
  };
}

LEVELS.forEach((vus, i) => {
  scenarios[`vu_${vus}`] = {
    executor: 'constant-vus',
    vus: vus,
    duration: `${STEP}s`,
    startTime: `${WARMUP + i * STEP}s`,
    tags: { level: String(vus) },
    exec: 'search',
  };

  // 항상 통과하는 임계값을 걸어 태그별 서브메트릭을 만든다.
  // k6 는 이렇게 해야 handleSummary 에서 단계별 값을 꺼낼 수 있다.
  thresholds[`http_req_duration{level:${vus}}`] = ['p(95)>=0'];
  thresholds[`http_reqs{level:${vus}}`]         = ['count>=0'];
  thresholds[`http_req_failed{level:${vus}}`]   = ['rate>=0'];
  thresholds[`sli_700ms{level:${vus}}`]         = ['rate>=0'];
});

export const options = { scenarios, thresholds, summaryTrendStats: ['avg', 'p(95)', 'p(99)', 'max'] };

export function search() {
  // VU 번호와 반복 횟수를 섞어 풀 전체에 퍼뜨린다.
  // VU 번호만 쓰면 동시 사용자가 적을 때 앞쪽 몇 명만 반복 조회되어
  // 그 유저들의 데이터만 캐시에 올라간다.
  const token = POOL[(__VU - 1 + __ITER) % POOL.length];

  const res = http.get(`${BASE}/page/search?keyword=${KEYWORD}`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { name: 'page_search' },
  });

  check(res, { 'status 200': (r) => r.status === 200 });
  sli.add(res.timings.duration < 700);
}

function pad(v, n) {
  const s = String(v);
  return s + ' '.repeat(Math.max(0, n - s.length));
}

function ms(v) {
  return v === undefined ? '-' : `${v.toFixed(1)}ms`;
}

function pct(v) {
  return v === undefined ? '-' : `${(v * 100).toFixed(2)}%`;
}

export function handleSummary(data) {
  const lines = [];
  lines.push('');
  lines.push(`대상 ${BASE} | 검색어 ${KEYWORD} | 사용자군 ${USERS} (${POOL.length}명) | 단계당 ${STEP}초 | 워밍업 ${WARMUP}초 × ${WARMUP_VUS}VU (측정 제외)`);
  lines.push('');
  lines.push(
    pad('동시요청', 10) + pad('SLI(700ms)', 12) + pad('p95', 11) +
    pad('p99', 11) + pad('최장응답', 11) + pad('처리량', 11) + pad('실패율', 9)
  );
  lines.push('-'.repeat(75));

  let limit = null;

  for (const vus of LEVELS) {
    const dur  = data.metrics[`http_req_duration{level:${vus}}`];
    const reqs = data.metrics[`http_reqs{level:${vus}}`];
    const fail = data.metrics[`http_req_failed{level:${vus}}`];
    const s    = data.metrics[`sli_700ms{level:${vus}}`];

    const sliRate = s && s.values ? s.values.rate : undefined;
    const rps     = reqs && reqs.values ? reqs.values.count / STEP : undefined;

    if (limit === null && sliRate !== undefined && sliRate < 0.95) limit = vus;

    lines.push(
      pad(vus, 10) +
      pad(pct(sliRate), 12) +
      pad(ms(dur && dur.values ? dur.values['p(95)'] : undefined), 11) +
      pad(ms(dur && dur.values ? dur.values['p(99)'] : undefined), 11) +
      pad(ms(dur && dur.values ? dur.values.max : undefined), 11) +
      pad(rps === undefined ? '-' : `${rps.toFixed(1)}/s`, 11) +
      pad(pct(fail && fail.values ? fail.values.rate : undefined), 9)
    );
  }

  lines.push('');
  if (limit === null) {
    lines.push(`모든 구간에서 SLI 95% 이상. 한계를 찾으려면 LEVELS 를 더 올릴 것.`);
  } else {
    lines.push(`동시 ${limit} 명에서 SLI 가 95% 아래로 떨어졌다. 그 직전이 수용 한계다.`);
  }
  lines.push('');

  return {
    stdout: lines.join('\n'),
    'summary.json': JSON.stringify(data, null, 2),
  };
}
