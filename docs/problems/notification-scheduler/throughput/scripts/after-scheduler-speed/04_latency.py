#!/usr/bin/env python3
"""동시 발송 로그에서 실행별 처리 시간과 실제 동시성을 뽑는다.

쓰는 법
    docker compose logs app --since 30m > run-n4.log
    python3 04_latency.py run-n4.log

retry/scripts/06_latency.py 와 뭐가 다른가
    그쪽은 발송시도 → 발송성공 을 순서대로 짝지었다. 스레드가 하나라 그래도 맞았다.
    동시에 보내면 여러 건의 로그가 섞여 나와서 그 방식은 엉뚱한 짝을 만든다.

    그래서 여기서는 **스레드 이름**으로 짝을 짓는다. 한 건은 처음부터 끝까지
    한 스레드가 맡으므로, 같은 스레드의 발송시도와 발송성공은 같은 건이다.

        [  alarm-send-3] [ALARM] 발송시도 alarmId=22003 ...
        [  alarm-send-3] [FCM] 전송성공 ...
        [  alarm-send-3] [ALARM] 발송성공 alarmId=22003 ...

무엇을 보나
    처리 시간   조회 → 실행완료.  이 값이 이 테스트의 답이다
    동시성      실제로 몇 개 스레드가 움직였는지. 설정한 N 과 같아야 한다
    1건 전체    한 건이 처음부터 끝까지 걸린 시간. N 을 올려도 안 변해야 정상
    FCM 구간    그중 FCM 왕복이 차지하는 몫
"""

import re
import sys

from datetime import datetime

TS = re.compile(r"(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}[.,]\d+)")
# 로그 앞머리의 [   alarm-send-3] 같은 스레드 칸. 컬러 코드가 섞여 있어 넉넉히 잡는다.
THREAD = re.compile(r"\[\s*([A-Za-z][\w-]*-\d+)\s*\]")
FETCH = re.compile(r"\[ALARM\] 조회.*?count=(\d+)")
FINISH = re.compile(r"\[ALARM\] 실행완료")
ALARM_END = ("발송성공", "발송포기", "재시도예약", "재시도소진", "발송실패")


def parse_time(line):
    m = TS.search(line)
    if not m:
        return None
    raw = m.group(1).replace(",", ".").replace(" ", "T")
    try:
        return datetime.fromisoformat(raw)
    except ValueError:
        return None


def thread_of(line):
    # 타임스탬프 뒤쪽만 본다. 앞에는 날짜의 대괄호가 없지만 컬러 코드가 있다.
    m = THREAD.search(line)
    return m.group(1) if m else None


class Run:
    def __init__(self, at, count):
        self.at, self.count = at, count
        self.started_at = at
        self.ended_at = None
        self.fcm, self.total = [], []
        self.ok = self.fail = self.skip = 0
        self.threads = set()
        # 스레드마다 지금 처리 중인 건의 시작 시각
        self._open = {}

    def attempt(self, th, t):
        self.threads.add(th)
        self._open[th] = t

    def fcm_end(self, th, t, kind):
        setattr(self, kind, getattr(self, kind) + 1)
        s = self._open.get(th)
        if s:
            self.fcm.append((t - s).total_seconds() * 1000)

    def done(self, th, t):
        s = self._open.pop(th, None)
        if s:
            self.total.append((t - s).total_seconds() * 1000)

    @property
    def elapsed(self):
        if self.ended_at and self.ended_at > self.started_at:
            return (self.ended_at - self.started_at).total_seconds()
        return None


def stats(xs):
    if not xs:
        return None
    g = sorted(xs)
    n = len(g)
    return {
        "n": n,
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
                count = int(m.group(1))
                cur = Run(parse_time(line), count) if count else None
                if cur:
                    runs.append(cur)
                continue

            if cur is None:
                continue

            t = parse_time(line)
            if t is None:
                continue

            if FINISH.search(line):
                cur.ended_at = t
                cur = None
                continue

            th = thread_of(line)
            if th is None:
                continue

            if "발송시도" in line:
                cur.attempt(th, t)
            elif "전송성공" in line:
                cur.fcm_end(th, t, "ok")
            elif "전송실패" in line or "[FCM] 예외" in line:
                cur.fcm_end(th, t, "fail")
            elif "건너뜀" in line:
                cur.fcm_end(th, t, "skip")
            elif any(k in line for k in ALARM_END):
                cur.done(th, t)

    if not runs:
        print("발송한 실행이 없다. '[ALARM] 조회 ... count=' 줄이 로그에 있는지 확인한다.")
        return

    print(f"발송한 실행 {len(runs)}개\n")

    for k, r in enumerate(runs, 1):
        when = r.at.strftime("%H:%M:%S") if r.at else "?"
        print(f"[{k}] {when}   조회 {r.count}건   스레드 {len(r.threads)}개")
        print(f"    성공 {r.ok} · 실패 {r.fail} · 건너뜀 {r.skip}")

        e = r.elapsed
        st = stats(r.total)
        fc = stats(r.fcm)

        if fc:
            print(f"    FCM 구간   중앙값 {fc['p50']:.0f} ms · p95 {fc['p95']:.0f} ms")
        if st:
            print(f"    1건 전체   중앙값 {st['p50']:.0f} ms · p95 {st['p95']:.0f} ms"
                  f" · {st['min']:.0f}~{st['max']:.0f} ms")
        if e:
            n = st["n"] if st else r.count
            print(f"    처리       {e:.1f} 초  ·  분당 {n / e * 60:.0f} 건")
            if st:
                # 리틀의 법칙으로 되짚어 본 값. 스레드 개수와 비슷해야 맞는 측정이다.
                print(f"    실효 동시성 {n / e * (st['p50'] / 1000):.1f}"
                      f"  (스레드 {len(r.threads)}개)")
        else:
            print("    처리       아직 안 끝났다 ('[ALARM] 실행완료' 줄이 없다)")
        print()

    if len(runs) > 1:
        print("실행별 요약")
        print(f"    {'조회':>6} {'스레드':>6} {'처리(초)':>9} {'1건 p50':>8}"
              f" {'1건 p95':>8} {'분당':>7}")
        for r in runs:
            st, e = stats(r.total), r.elapsed
            if not st or not e:
                continue
            print(f"    {r.count:>6} {len(r.threads):>6} {e:>9.1f}"
                  f" {st['p50']:>7.0f}m {st['p95']:>7.0f}m {st['n'] / e * 60:>7.0f}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    main(sys.argv[1])
