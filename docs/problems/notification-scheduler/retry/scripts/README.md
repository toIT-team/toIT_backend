# 측정 스크립트

> 본문: [알림 유실 해결기](../README.md) · 결과: [measurements.md](../measurements.md)

`is_sent` 불리언을 `status` 로 바꾸면서 데이터를 만들고 세는 방법이 달라졌다.
같은 파일을 고쳐 쓰면 개선 전 측정을 다시 못 하므로 두 벌로 나눠 두었다.

```
before/    개선 전.  is_sent 기준
after/     개선 후.  status · attempt_count · next_attempt_at 기준
07_migrate.sql   before → after 로 넘어갈 때 한 번 실행
04 · 05 · 06     양쪽 공용
```

---

## 어느 쪽을 쓸 것인가

```
schedules_alarm 에 status 컬럼이 있다        after/
없다                                       before/
```

`00_check_schema.sql` 로 확인할 수 있다.

---

## 한 라운드

```
1  04_teardown.sql          앞 라운드 데이터를 지운다
2  <폴더>/01_setup.sql       @USERS_ID · @COUNT 를 맞추고 실행
3  <폴더>/02_reset.sql       @AFTER_MIN 으로 실험을 고른다
4  05_metrics.sql           실험 전 스냅샷
5  대기 — 알림이 나간다
6  05_metrics.sql           QPS · TPS · 건당 쿼리
7  <폴더>/03_count.sql       도달 · 미도달 집계
8  로그를 받아 지연을 뽑는다
```

```bash
docker compose logs app --since 30m > app.log
python3 06_latency.py app.log
```

한계를 찾을 때는 `@COUNT` 를 100 → 200 → 300 → 500 으로 올려가며 1번부터 반복한다.

---

## 파일

| 파일 | 하는 일 |
|---|---|
| `00_check_schema.sql` | 컬럼 이름이 스크립트와 맞는지 확인 |
| `01_setup.sql` | 일정과 알림 예약을 `@COUNT` 건 만든다 |
| `02_reset.sql` | 알림 시각을 모으고 상태를 초기화한다 |
| `03_count.sql` | 도달·미도달 집계 |
| `04_teardown.sql` | 테스트 데이터와 `metrics_snapshot` 정리 |
| `05_metrics.sql` | QPS·TPS. 발송 앞뒤로 두 번 실행 |
| `06_latency.py` | 로그에서 실행별 지연과 처리량을 뽑는다 |
| `07_migrate.sql` | 컬럼·인덱스 추가. **배포보다 먼저** 실행 |

---

## 주의

**측정 중에는 아무도 앱을 쓰지 않게 한다.** `05_metrics.sql` 이 읽는 것은 MySQL
전역 카운터라 다른 요청도 함께 세어진다.

**로그 파일은 커밋하지 않는다.** 사용자 번호와 일정 번호가 들어 있다.
`.gitignore` 에 이미 걸려 있다.
