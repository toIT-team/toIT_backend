# 부하 테스트 자산

목적별로 나눠 둔다. SQL 은 모두 **테스트 서버 전용**이며 운영에서 실행하지 않는다.

```
common/        유저·토큰 등 공통 준비물
search/        통합검색 성능 · 서버 수용 인원 측정
storage-limit/ 스토리지 5GB 제한의 경쟁 조건 재현
_postgres/     MySQL 전환(2026-08) 이전 버전. 참고용, 실행하지 않음
```

> `capacity_test.js`(서버가 동시 몇 명까지 버티나)와 `storage-limit`(사용자당 5GB 제한)은
> 둘 다 "용량"이라는 말을 쓰지만 다른 축이다. 폴더로 구분해 둔다.

## 접속 정보

DB 는 SSH 터널을 통해 붙는다.

```bash
ssh -i <키> -L 3306:127.0.0.1:3306 ubuntu@<테스트서버IP>
```

부하는 **NGINX 를 우회해 앱 포트로 직접** 보낸다. Rate Limiter(`limit_req`)가
요청을 잘라내면 애플리케이션 레벨 측정이 안 되기 때문.

```
http://<테스트서버IP>:8080     ← 측정용 (Rate Limiter 우회)
https://test.toit.cloud        ← Rate Limiter 포함 경로
```

---

## common — 먼저 준비

| 파일 | 역할 | 멱등 |
|---|---|---|
| `00_numbers_mysql.sql` | `generate_series` 대체용 숫자 테이블(1~10000). **한 번만** | O |
| `01_create_users_mysql.sql` | loadtest 유저 30명 생성 (`loadtest{n}@toit.local`) | O |
| `03_teardown_mysql.sql` | loadtest 유저와 그 데이터 전체 삭제 | — |
| `gen_tokens.py` | 유저별 JWT 발급 → `tokens.json` | O |

```bash
pip install pyjwt
python3 gen_tokens.py            # UID_START, UID_END 확인 후 실행
```

`tokens.json` 은 커밋하지 않는다.

## search — 검색 성능 · 수용 인원

| 파일 | 역할 |
|---|---|
| `02_seed_data_mysql.sql` | loadtest 유저 전원에게 더미 데이터 (테이블당 1,000행) |
| `seed_search_mysql.sql` | 특정 유저 1명에게 대량 시드 (기본 10,000행) |
| `search_loadtest.js` | 통합검색 부하 |
| `capacity_test.js` | 동시 사용자 수를 올려가며 SLI 측정 |
| `capacity_fine.js` | 수용 한계 부근을 촘촘히 측정 |

`02_seed_data_mysql.sql` 은 **멱등이 아니다.** 재시드 전에 `common/03_teardown_mysql.sql` 을 먼저 실행할 것.

## storage-limit — 5GB 제한 경쟁 조건

| 파일 | 역할 |
|---|---|
| `setup.sql` | 대상 유저의 남은 용량을 10MB 로 좁힌다 |
| `teardown.sql` | filler 행과 테스트로 생긴 예약(PENDING) 제거 |

`setup.sql` 상단의 `users_id`, `folders_id`, 남길 여유 값을 직접 수정한 뒤 실행한다.
사용자 변수(`@v_...`)를 쓰지 않으므로 DBeaver 에서 문 단위로 실행해도 동작한다.

### 측정 시나리오

**1) 순차 반복 — 차단되어야 함**

남은 용량보다 큰 요청을 연달아 보낸다. 예약(PENDING)이 용량을 선점하므로
남은 용량을 넘는 순간부터 `스토리지 용량이 초과되었습니다` 가 나와야 한다.

**2) 동시 요청 — 현재는 뚫림**

같은 사용자로 동시에 N 건을 보낸다. `SUM 조회 → PENDING INSERT` 사이가 비어 있어
여러 요청이 같은 사용량을 읽고 전부 통과한다. 몇 건이 통과했는지, 최종 사용량이
5GB 를 얼마나 넘겼는지를 기록한다.

> 용량 제한은 **사용자 단위**다. 서로 다른 사용자끼리는 각자의 SUM 만 보므로 경쟁하지 않는다.
> 재현하려면 **같은 사용자**로 동시 요청을 보내야 한다.

테스트 후 `teardown.sql` 로 정리한다.
