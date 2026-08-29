#!/usr/bin/env python3
"""로그에서 스케줄러 실행별 소요 시간과 처리량을 뽑는다.

쓰는 법
    docker compose logs app --since 30m > app.log
    python3 06_latency.py app.log

어떻게 나누나
    스케줄러는 실행마다 이 줄을 찍는다.

        [ALARM] 조회 window=2026-08-28T18:17~18:18 count=100

    이 줄을 경계로 삼아 실행을 나눈다. 그래서 한 파일에 100건·300건·500건
    라운드가 섞여 있어도 알아서 갈린다. count=0 인 실행은 건너뛴다.

무엇을 재나
    FCM 구간   [ALARM] 발송시도  →  [FCM] 전송성공 / 전송실패 / 건너뜀
    1건 전체   [ALARM] 발송시도  →  [ALARM] 발송성공 / 발송포기 / 재시도예약 / 재시도소진
               (개선 전 로그에서는 [ALARM] 완료표시)
"""

import re
import sys

from datetime import datetime

TS = re.compile(r"(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}[.,]\d+)")
FETCH = re.compile(r"\[ALARM\] 조회.*?count=(\d+)")
FCM_END = ("전송성공", "전송실패", "건너뜀")
# 알림 한 건이 끝났음을 알리는 줄. 개선 전후로 이름이 다르다.
ALARM_END = ("완료표시", "발송성공", "발송포기", "재시도예약", "재시도소진")


def parse_time(line):
    m = TS.search(line)
    if not m:
        return None
    raw = m.group(1).replace(",", ".").replace(" ", "T")
    try:
        return datetime.fromisoformat(raw)
    except ValueError:
        return None


class Run:
    def __init__(self, at, count):
        self.at, self.count = at, count
        self.fcm, self.total = [], []
        self.ok = self.fail = self.skip = 0
        self.first = self.last = None
        self._started = None

    def attempt(self, t):
        self._started = t
        self.first = self.first or t

    def fcm_end(self, t, kind):
        setattr(self, kind, getattr(self, kind) + 1)
        if self._started:
            self.fcm.append((t - self._started).total_seconds() * 1000)

    def done(self, t):
        if self._started:
            self.total.append((t - self._started).total_seconds() * 1000)
            self._started = None
        self.last = t

    @property
    def elapsed(self):
        if self.first and self.last and self.last > self.first:
            return (self.last - self.first).total_seconds()
        return None


def stats(gaps):
    if not gaps:
        return None
    g = sorted(gaps)
    n = len(g)
    return {
        "n": n,
        "avg": sum(g) / n,
        "p50": g[n // 2],
        "p95": g[min(int(n * 0.95), n - 1)],
        "min": g[0],
        "max": g[-1],
    }


def main(path):
    runs, cur = [], None

    with open(path, encoding="utf-8", errors="replace") as f:
        for line in f:
            m = FETCH.search(line)
            if m:
                t = parse_time(line)
                count = int(m.group(1))
                cur = Run(t, count) if count else None
                if cur:
                    runs.append(cur)
                continue

            if cur is None:
                continue
            t = parse_time(line)
            if t is None:
                continue

            if "발송시도" in line:
                cur.attempt(t)
            elif "전송성공" in line:
                cur.fcm_end(t, "ok")
            elif "전송실패" in line:
                cur.fcm_end(t, "fail")
            elif "건너뜀" in line:
                cur.fcm_end(t, "skip")
            elif any(k in line for k in ALARM_END):
                cur.done(t)

    if not runs:
        print("발송한 실행이 없다. '[ALARM] 조회 ... count=' 줄이 로그에 있는지 확인한다.")
        return

    print(f"발송한 실행 {len(runs)}개\n")

    for k, r in enumerate(runs, 1):
        st = stats(r.total) or stats(r.fcm)
        when = r.at.strftime("%H:%M:%S") if r.at else "?"
        print(f"[{k}] {when}   조회 {r.count}건")
        print(f"    성공 {r.ok} · 실패 {r.fail} · 건너뜀 {r.skip}")

        if r.skip and not r.ok and not r.fail:
            print("    → FCM 을 한 번도 안 탔다. 'FCM 제외' 기준이다.")

        f = stats(r.fcm)
        if f:
            print(f"    FCM 구간   중앙값 {f['p50']:.0f} ms · p95 {f['p95']:.0f} ms")
        if st:
            print(f"    1건 전체   중앙값 {st['p50']:.0f} ms · p95 {st['p95']:.0f} ms"
                  f" · {st['min']:.0f}~{st['max']:.0f} ms")

        e = r.elapsed
        if e and st:
            over = "  ← 1분 초과" if e > 60 else ""
            print(f"    처리       {e:.1f} 초{over}")
            print(f"    RPS        {st['n'] / e:.1f} 건/초  ·  분당 {st['n'] / e * 60:.0f} 건")
        print()

    if len(runs) > 1:
        print("건수별 요약")
        print(f"    {'조회':>6}  {'처리(초)':>9}  {'중앙값':>7}  {'p95':>7}  {'RPS':>6}")
        for r in runs:
            st = stats(r.total) or stats(r.fcm)
            e = r.elapsed
            if not st or not e:
                continue
            print(f"    {r.count:>6}  {e:>9.1f}  {st['p50']:>6.0f}m  {st['p95']:>6.0f}m"
                  f"  {st['n'] / e:>6.1f}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    main(sys.argv[1])
