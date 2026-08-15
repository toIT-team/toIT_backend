# 통합검색 부하 테스트 데이터 (MySQL)

`scripts/` 의 옛 시드를 대체한다. 원본 PostgreSQL 측정과 조건을 맞추기 위해 두 가지를 고쳤다.

```
헤비 유저          없었음        →  loadtest30 이 10,000행씩 보유
검색 테이블당 행 수  30,000       →  약 39,000    (원본과 일치)
검색어             모든 행에 매칭 →  테이블당 10행만 매칭
유저당 검색 결과    1,000건       →  50건         (원본과 일치)
```

옛 시드는 모든 행에 `loadtest` 가 들어가 있어 `LIKE` 가 아무것도 걸러내지 못했다.
필터 비용이 측정에서 빠져 원본과 다른 조건이 된다.

## 실행 순서

```bash
D() { docker exec -i -e MYSQL_PWD="$MYSQL_ROOT_PW" toit-test-mysql \
        mysql -uroot --table toit_test; }

D < 03_teardown_mysql.sql      # 기존 loadtest 데이터 전부 삭제
D < 00_numbers_mysql.sql       # seq_numbers 1~10000 (멱등, 한 번만)
D < 01_create_users_mysql.sql  # loadtest 유저 30명 (멱등)
D < 02_seed_mysql.sql          # 더미 데이터
```

`02_seed_mysql.sql` 은 **멱등이 아니다.** 다시 넣으려면 `03_teardown` 부터 다시 돈다.

## 구성

| 파일 | 하는 일 | 멱등 |
| --- | --- | --- |
| `00_numbers_mysql.sql` | `seq_numbers` 보조 테이블 (1~10,000) | ✅ |
| `01_create_users_mysql.sql` | `loadtestN@toit.local` 유저 30명 | ✅ |
| `02_seed_mysql.sql` | 5개 테이블 더미 데이터 | ❌ |
| `03_teardown_mysql.sql` | loadtest 유저와 그 데이터 전부 삭제 | ✅ |

`03_teardown` 은 이메일이 `loadtest%@toit.local` 인 유저만 지운다. 기존 유저는 건드리지 않는다.

## 데이터 구성

| | 일반 유저 29명 | 헤비 유저 1명 | 상한 근거 |
| --- | --- | --- | --- |
| folders | 100 | 100 | 앱에서 유저당 100개 제한 |
| schedules | 1,000 | 10,000 | 없음 |
| links | 1,000 | 10,000 | 없음 |
| texts | 1,000 | 10,000 | 없음 |
| attachments | 1,000 | 5,000 | 총 5GB, 평균 1MB 가정 |
| **합계** | **4,100** | **35,100** | |

검색 대상 테이블은 `29 × 1,000 + 10,000 = 39,000` 행이 된다.

## 검색어

```
zzsearch
```

각 테이블에서 `n <= 10` 인 행에만 심는다. 5개 테이블 × 10 = **유저당 50건**.
나머지 행은 `item ...` 으로 들어가 매칭되지 않는다.

> 옛 시드의 검색어는 `loadtest` 였다. k6 스크립트가 그 값을 쓰고 있으면 `zzsearch` 로 바꿔야 한다.

## 확인

`02_seed_mysql.sql` 끝에 확인 쿼리 세 개가 붙어 있다.

```
1. 테이블별 총 행 수         schedules 약 39,000
2. 유저당 검색 결과 건수      각 열 10, 합계 50
3. 유저별 보유 행 수 상위 3   헤비 35,100 / 일반 4,100
```

## 변수

`02_seed_mysql.sql` 위쪽에서 바꿀 수 있다.

```sql
SET @v_n_normal = 1000;                      -- 일반 유저 테이블당 행 수
SET @v_n_heavy  = 10000;                     -- 헤비 유저 테이블당 행 수
SET @v_n_heavy_attach = 5000;                -- 헤비 유저 첨부 (5GB 상한)
SET @v_kw_rows  = 10;                        -- 테이블당 검색어 포함 행 수
SET @v_heavy    = 'loadtest30@toit.local';   -- 헤비 유저
```

`@v_n_heavy` 를 10,000 보다 크게 하려면 `seq_numbers` 를 먼저 늘려야 한다.
