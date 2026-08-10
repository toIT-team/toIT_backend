# 2GB 서버에 6개 서비스를 욱여넣기 — Docker Compose 도입기

> NGINX, Spring, PostgreSQL, Grafana Alloy가 돌아가는 2GB 단일 서버에
> Redis와 CI/CD를 더하면서, 왜 Docker Compose를 선택했는지에 대한 기록.

## 들어가며

우리 서버는 2GB 메모리 한 대다. 화려한 스펙이 아니다. 그런데 서비스는 계속 늘었다.
NGINX, Spring Boot, PostgreSQL, 그리고 관측성을 위한 Grafana Alloy까지.
여기에 검색 성능을 위한 Redis와 CI/CD 파이프라인까지 붙이려는 시점이 됐다.

서비스가 이 정도로 늘어나니, "서버에 그때그때 손으로 설치하고 실행하는" 방식이 한계에 부딪혔다.
이 글은 그 한계를 어떻게 느꼈고, 왜 Docker와 Docker Compose를 선택했으며,
**2GB라는 제약 안에서 어떤 선택을 했는지**에 대한 기록이다.

미리 말하면, 이 글의 정체성은 하나다 — **"제약 안에서 합리적으로 고른 이야기"**.
모든 기술 결정은 "2GB"라는 축으로 꿰어진다.

---

## 1. 왜 Docker인가

Docker의 장점은 인터넷에 널려 있으니, 우리가 **실제로 필요했던 이유**만 적는다.

### 서비스가 늘어나면 수동 관리가 무너진다

서버 하나에 다음을 전부 직접 설치한다고 생각해보자.

- NGINX 설치 · 설정
- JDK 설치 + Spring 실행 스크립트
- PostgreSQL 설치 · 버전 관리
- Grafana Alloy 바이너리
- (예정) Redis

버전이 충돌하고, 설정 파일이 서버 곳곳에 흩어지고,
"이 서버에 뭐가 깔려 있더라"를 기억에 의존하게 된다.
서비스가 1~2개면 그냥 설치해도 되지만, **6개쯤 되면 격리와 조합이 필요한 임계점**이 온다.

Docker는 각 서비스를 컨테이너로 격리하고, 그 실행 환경을 이미지 하나에 담는다.
"이 서버에 뭐가 깔려 있는가"가 기억이 아니라 **코드(`docker-compose.yml`)로 남는다.**

### CI/CD의 배포 단위가 필요했다

사실 Docker 도입의 가장 정직한 방아쇠는 CI/CD였다.

CI/CD를 제대로 하려면 "빌드 산출물"을 서버로 옮겨 실행해야 하는데,
그 산출물이 JAR 파일이면 여전히 "서버에 JDK 버전 맞고, 환경변수 맞고…"를 신경 써야 한다.
반면 **Docker 이미지는 실행 환경까지 통째로 담은 배포 단위**다.

```
CI에서  docker build  →  레지스트리에 push
서버에서  docker pull  →  docker compose up -d
```

"CI에선 됐는데 서버에선 안 됨"이 구조적으로 사라진다.
롤백도 이전 이미지 태그로 다시 `up` 하면 끝이다.

### 로컬 = 운영 환경 일치

새로 Redis를 도입할 때 이 이점이 특히 와닿았다.
Compose 파일에 몇 줄 추가하는 것만으로 **로컬에도 운영과 같은 버전의 Redis가 뜬다.**
"내 로컬에 Redis 깔고 버전 맞추고…" 하는 온보딩 비용이 사라진다.

---

## 2. 왜 Docker Compose인가

Docker와 Compose를 도입 이유를 섞으면 논리가 흐려진다. 나눠서 보자.

- **Docker(컨테이너화)를 왜?** → 환경을 코드로 고정하고, 어디서든 똑같이 뜨게 만든다.
- **Compose를 왜?** → 그 컨테이너가 여러 개(NGINX + Spring + PG + Redis + Alloy)니까, **한 파일로 묶어 한 번에 관리**한다.

Compose는 다중 서비스 스택을 하나의 선언적 파일로 정의하고,
`docker compose up` 명령 하나로 전체를 재현한다.
서비스 간 통신도 서비스명으로 해결되고(`db:5432`, `redis:6379`),
기동 순서(`depends_on`), 전용 네트워크, 볼륨, **리소스 상한**까지 전부 코드로 남는다.

특히 마지막 "리소스 상한"이 우리에겐 생명줄이었다. 다음 챕터로 이어진다.

---

## 3. 2GB와의 싸움 (이 글의 핵심)

서비스를 컨테이너로 나누는 것과 별개로, **총 메모리는 여전히 2GB다.**
현실적인 예산을 짜봤다.

| 컴포넌트 | 대략 RAM |
|---|---|
| OS + Docker 데몬 | 200~400MB |
| Spring Boot (JVM) | 400~700MB |
| PostgreSQL | 200~400MB |
| NGINX | 20~50MB |
| Grafana Alloy | 100~200MB |
| Redis (캐시) | 50~150MB |

다 더하면 **1.2~1.8GB.** 2GB에서 돌아가긴 하지만 여유가 거의 없다.
트래픽이 튀거나 JVM GC가 돌 때 OOM으로 컨테이너가 죽을 수 있다.
그래서 다음 세 가지를 안전장치로 걸었다.

### ① 스왑(swap) — 안전벨트

2GB에선 스왑이 사실상 필수다. 순간 스파이크에 프로세스가 즉사하는 걸 막는다.

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# /etc/fstab 에 등록해 재부팅 후에도 유지
```

### ② JVM 힙 명시적 제한

컨테이너 안의 JVM은 호스트 전체 메모리를 보고 힙을 크게 잡으려다 터진다.
반드시 힙을 명시한다.

```yaml
spring:
  environment:
    - JAVA_OPTS=-Xms256m -Xmx512m -XX:MaxMetaspaceSize=128m
  mem_limit: 700m
```

### ③ 서비스별 메모리 상한

한 놈이 폭주해 전체를 끌어내리는 걸 막는다.

```yaml
postgres:
  mem_limit: 400m
redis:
  mem_limit: 150m
  command: redis-server --maxmemory 120mb --maxmemory-policy allkeys-lru
```

> **여담:** 2GB라는 제약이 오히려 좋은 글감이 됐다.
> 넉넉한 스펙에서 마음껏 쓴 이야기보다, 제약 안에서 예산을 짜고 튜닝한 이야기가 더 실전적이다.

---

## 4. 관측성 — Grafana Alloy와 컨테이너 로그

처음엔 "로깅이 편해지니까 Docker를 쓰자"고 막연히 생각했다.
그런데 정확히 말하면 **로깅은 Docker의 고유 장점이라기보다, 컨테이너화가 로그 수집을 표준화해주는 것**이다.

우리는 이미 Grafana Alloy로 관측성 파이프라인을 두고 있다.
서비스마다 로그 파일 경로가 제각각이면 수집이 번거롭다.
컨테이너는 **표준출력(stdout)** 으로 로그를 뱉는 게 관례라,
Alloy가 Docker를 통해 모든 컨테이너 로그를 일관되게 긁어갈 수 있다.

즉 "컨테이너화 = 로그가 stdout으로 통일 = Alloy 설정 단순화".
처음 꽂혔던 "로깅"은 이렇게 **관측성 파이프라인의 일부**로 자리 잡았다.

---

## 5. 통합 검색 — PostgreSQL로 검색, Redis로 캐싱

우리 서비스엔 folders 안의 texts / links / attachments를 가로지르는 통합 검색이 있다.
검색을 강화하려니 두 가지 선택지가 떠올랐다.

1. 별도 검색 엔진(Elasticsearch, RediSearch 등)을 도입한다
2. 이미 있는 PostgreSQL의 검색 기능을 쓴다

**2GB 제약에서 답은 명확했다 — PostgreSQL.**

별도 검색 엔진은 인덱스를 통째로 메모리에 올리는 경우가 많아,
2GB에 PostgreSQL과 동거하는 상황에선 메모리가 못 버틴다.
그리고 PostgreSQL은 그 자체로 충분히 강력한 검색기다.

### 한글 검색은 pg_bigm

우리 검색 대상은 한글 위주다.
PostgreSQL 기본 Full-Text Search(`tsvector`)는 형태소 분석 사전에 의존해 한글 부분 일치에 약한 편이라,
**bigram 기반의 `pg_bigm` 확장**을 선택했다.
`pg_bigm`은 두 글자 단위로 인덱싱해 한글 부분 문자열 검색(`LIKE '%검색어%'`)을 GIN 인덱스로 가속한다.

```sql
CREATE EXTENSION pg_bigm;

CREATE INDEX idx_texts_content_bigm
  ON texts USING gin (content gin_bigm_ops);

-- 부분 일치 검색이 인덱스를 탄다
SELECT * FROM texts WHERE content LIKE '%검색어%';
```

### Redis는 "검색 결과 캐시"로만

Redis는 검색 엔진이 아니라, **자주 나오는 검색 결과를 잠깐 저장하는 캐시**로만 쓴다.
실제 검색은 PostgreSQL이 하고, 반복 요청은 Redis가 빠르게 돌려준다. 역할이 명확히 갈린다.

```yaml
redis:
  image: redis:7-alpine
  command: redis-server --maxmemory 120mb --maxmemory-policy allkeys-lru
  mem_limit: 150m
```

- **캐시 키**: 검색어 + 필터 + 페이지 (예: `search:folder123:query:page1`)
- **TTL**: 5~10분. 캐시는 날아가도 되는 데이터라 안정성 부담이 없다(miss 나면 PG가 다시 계산).
- **무효화**: 정교한 무효화 대신 **TTL 자연 만료**로 단순하게. 이 규모에선 그게 정답이다.
- **`allkeys-lru`**: 메모리가 차면 오래된 캐시부터 자동 삭제 → 2GB를 절대 위협하지 않는다.

Redis가 검색 인덱스를 들고 있지 않기 때문에, 데이터가 커져도 Redis 메모리는 상한 안에서 논다.

---

## 6. 스케일링에 대한 솔직한 이야기

"Docker Compose로 스케일링도 할 수 있지 않을까?"라고 생각했었다.
결론부터 말하면 — **Compose는 스케일링 도구가 아니다.** 여기서 솔직해질 필요가 있다.

Compose에도 `docker compose up --scale spring=3` 같은 복제 기능은 있다.
하지만 이건 근본적으로 **단일 호스트(서버 한 대) 안에서의 복제**다. 한계가 명확하다.

- 서버 한 대의 CPU/메모리를 넘지 못한다 (우리는 그 한 대가 2GB다)
- 로드밸런싱을 앞단 NGINX가 직접 해줘야 한다
- 오토스케일링, 셀프 힐링, 무중단 롤링 업데이트는 Compose의 영역이 아니다

진짜 수평 확장은 Kubernetes, Docker Swarm, ECS 같은 오케스트레이터의 몫이다.
게다가 우리 서버는 2GB라, Spring 인스턴스를 여러 개 띄우는 것 자체가 메모리상 불가능하다.

그래서 스케일링은 **지금 실행하는 기능이 아니라 "설계와 로드맵"으로만** 둔다.
다만 지금부터 준비할 수 있는 건 있다.

- **무상태(stateless) 설계**: 세션 같은 상태를 앱 메모리가 아니라 외부(Redis 등)로 뺀다.
- **컨테이너 이미지화**: 이미 하고 있다.

이 두 가지를 잡아두면, 나중에 트래픽이 커져서 서버를 늘리거나 오케스트레이터로 옮길 때
**애플리케이션은 거의 그대로 이사 간다.** 지금은 그 발판을 만드는 단계다.

> "Compose로 다 된다"고 쓰는 것보다,
> "Compose로 여기까지, 그 다음은 이렇게"라고 한계를 인정하는 편이 더 정직하고 프로답다.

---

## 7. CI/CD 파이프라인

마지막으로, 이 모든 걸 하나로 묶는 CI/CD.

```
[GitHub Push]
     │
     ▼
[CI]  테스트 → docker build → 레지스트리에 push
     │
     ▼
[서버]  docker compose pull → docker compose up -d
```

Docker 이미지를 배포 단위로 삼으니, CI/CD가 단순해졌다.
빌드 결과가 곧 실행 환경이라, 배포 후 "환경이 달라서" 나는 문제가 줄었다.
롤백도 이전 태그로 `up` 하면 끝이다.

---

## 마치며

돌아보면 모든 결정이 **"2GB 단일 서버"라는 하나의 제약**에서 나왔다.

- 서비스가 늘어 수동 관리가 한계 → **Docker**
- 6개 서비스를 한 파일로, 리소스 상한까지 → **Compose**
- 메모리가 빠듯해서 → JVM 힙 제한, mem_limit, swap
- 검색 엔진을 따로 못 두니 → **PostgreSQL(pg_bigm) 검색 + Redis 캐시**
- 스케일아웃은 못 하지만 → **무상태 설계로 발판만 마련**

제약이 오히려 선택을 명확하게 만들어줬다.
넉넉한 스펙이었다면 고민 없이 이것저것 붙였을 텐데,
2GB라는 한계가 "정말 필요한 것"만 남기게 했다.

제약은 때로 가장 좋은 설계 가이드다.
