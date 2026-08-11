# 개선 대상 문제 목록

**동시성과 정합성** 한 축으로 모아 정리한다.

## 작업 순서

```
1. 스토리지 한도 — 남은 측정과 글             4~5일
2. 트랜잭션 경계 부재                        3~5일
3. 링크 프리뷰 — 스레드 점유와 SSRF           1~2주
```

**1·2 는 같은 축이다.** 스토리지는 격리(Isolation), 트랜잭션 경계는 원자성(Atomicity)으로
ACID 의 다른 글자를 다룬다. 나란히 두면 하나의 이야기가 된다.

**결과가 확실한 것부터 놓았다.** 1·2·3 은 문제가 코드에서 이미 확인되어
무엇이 나올지 알고 시작한다.

통합검색 MySQL 재측정은 여유가 될 때 한다. 해보기 전에는 결과를 알 수 없다.
CloudFront 서명 범위는 CDN 복구 작업과 묶는다.
알림 스케줄러는 기능이 꺼져 있어 보류한다.

---

## 1. 스토리지 한도 경쟁 조건 — 완료

→ [storage-limit/](storage-limit/)

업로드 URL 발급이 `사용량 합산 → 검증 → 예약 저장` 구조라 합산과 저장 사이에
다른 요청이 끼어들면 5GB 한도가 뚫렸다. 동시 2건부터 초과, 최대 135MB.

사용자 행에 `SELECT ... FOR UPDATE` 를 걸어 직렬화했다.
전 구간 초과 0, 락 대기 = 동시 요청 수 − 1 로 직렬화 검증, 실패·데드락 0.

**남은 것**

- 커넥션 풀을 5 / 20 으로 바꿔 "뚫리는 양의 상한을 풀 크기가 정한다" 가설을 확정한다.
  SERIALIZABLE 측정에서 데드락도 10 에서 포화했으므로 함께 확인된다.
- 원자적 UPDATE 는 아직 실측하지 않았다. 집계 정합성 관리 비용이 얼마인지 확인한다.
- 해결 과정을 글로 정리한다. `FOR UPDATE` 가 트랜잭션의 첫 DB 접근이 아니면
  무력해지는 것(스냅샷이 첫 일반 조회 시점에 확정됨)이 핵심이다.
- 구버전 업로드 경로(`createImagesInFolders` / `createFilesInFolders`)는 미적용.
- 업로드 크기를 클라이언트가 보낸 값으로 신뢰한다. 저장 확정에서 `HeadObject` 로 실측해야 한다.

## 2. 트랜잭션 경계 부재 — 미착수

`@Transactional` 이 하나도 없는 서비스가 5개다.
`LinksService` · `TextsService` · `FoldersService` · `SchedulesService` · `UserNotificationService`

Spring Data JPA 의 `save()` 에는 `@Transactional` 이 붙어 있어 **호출마다 개별 커밋**된다.

```java
// LinksService.createLinksInFolders — 검증과 저장이 루프 안에 섞여 있다
for (Long foldersId : foldersIdList) {
    foldersService.findByFoldersIdAndUsers_UsersId(usersId, foldersId);  // 권한·존재 검증
    linksRepository.save(item);                                          // 즉시 커밋
}
```

폴더 `[A, B, C]` 중 C 가 없으면 A·B 에는 저장된 채로 실패 응답이 나간다.
사용자가 재시도하면 A·B 에 중복 생성된다.

```java
// FoldersService.deleteFolders — 여섯 덩어리가 전부 별개 트랜잭션
schedulesRepository.saveAll(schedules);   // ①
textsRepository.saveAll(texts);           // ②
linksRepository.saveAll(links);           // ③
    ...
    if (activeCount == 0) s3Storage.delete(objectKey);   // ④ 롤백 불가
attachMentsRepository.saveAll(attachMents);              // ⑤
foldersRepository.save(folder);                          // ⑥
```

④ 이후 실패하면 **S3 파일은 영구 소실되고 DB 행은 ACTIVE 로 남는다.**
사용자 화면에는 보이는데 열면 깨지는 상태이며 복구할 수 없다.

첨부 100개인 폴더를 지우면 루프 안에서 `flush` 100번 + `count` 쿼리 100번이 나간다.

**참고** — 회원 탈퇴(`UsersWithdrawService`)에서는 S3 삭제를
`TransactionSynchronization.afterCommit` 에 등록해 DB 커밋이 확정된 뒤에만 실행한다.
같은 원칙이 폴더 삭제에는 적용되어 있지 않다.

**판단할 것** — S3 삭제를 트랜잭션 안에 두면 롤백해도 파일이 돌아오지 않고,
밖에 두면 커밋 후 실패 시 고아 객체가 남는다. 어느 오류가 덜 나쁜지 고른다.

**측정** — 부분 저장 건수, 재시도 시 중복 건수, S3 와 DB 불일치 건수, 폴더 삭제 시 쿼리 수

## 3. 링크 프리뷰 — 스레드 점유와 SSRF — 미착수

→ [link-preview/](link-preview/)

```
성능   timeout 4초 × User-Agent 2회 = 최악 8초 톰캣 스레드 점유 (max-threads 기본 200)
       외부 사이트의 응답 속도가 우리 처리량을 정한다
보안   URL 검증 코드 없음 + followRedirects(true)
       → http://169.254.169.254/latest/meta-data/ 등 내부 주소 접근 가능
```

리다이렉트를 따라가므로 정상 도메인으로 시작해 내부 주소로 넘어가는 경로도 열려 있다.
도메인만 검사하는 방어로는 막히지 않고 매 홉마다 검사해야 한다.

---

## 여유가 될 때 — 통합검색 MySQL 재측정

→ [search/](search/)

인덱스 최적화와 병목 제거를 **PostgreSQL 에서** 수행했다.
그 뒤 DB 를 MySQL 8.0 으로 전환했으므로 **현재 운영 환경에서의 값이 아니다.**

```
측정 시점   PostgreSQL   p95 984 → 292ms, 처리량 36 → 120 req/s, 수용 인원 20 → 50명
현재 운영   MySQL 8.0
```

전환 시 인덱스는 엔티티 코드로 이관해 그대로 살아 있다.
다만 옵티마이저가 다르므로 같은 인덱스가 같은 효과를 내는지는 확인되지 않았다.

**확인할 것**

- 복합 인덱스가 MySQL 에서도 같은 효과인가 (`EXPLAIN` 비교)
- PostgreSQL 에만 있는 접근 방식(Bitmap Index Scan 등)에 의존하던 부분은 없는가
- 숨은 S3 서명 생성 병목(CPU 42.6%)은 DB 와 무관하므로 그대로일 것으로 예상
- 수용 인원이 여전히 동시 50명인가

시드 스크립트는 MySQL 용으로 이미 준비되어 있다(`search/scripts/`).

## 정의만 해둔 것

### CloudFront 서명 범위

```java
// CloudFrontSigner.java:45
String resourceUrl = domain + "/*";
```

한 사용자에게 발급한 서명 쿠키로 **도메인 전체 파일에 접근할 수 있다.**
`domain + "/users/{userId}/*"` 로 좁혀야 한다.
CDN 이 현재 서빙 경로에서 빠져 있으므로 되살리기 전에 처리한다.

---

## 보류 — 알림 스케줄러

→ [notification-scheduler/](notification-scheduler/)

```java
if (isSent) { userNotificationService.markAsSent(notification); }
alarm.markAsSent();     // ← 조건 밖. 발송 실패해도 완료 처리되어 영구 유실
```
```sql
WHERE alarmDateTime >= :now AND alarmDateTime < :nextMinute   -- 그 1분만 조회
```

조회 창이 현재 1분뿐이라 서버가 잠깐만 멈춰도 그 사이 알림은 `isSent = false` 로 남은 채
다시 조회되지 않는다. 배포로 재시작할 때마다 발생한다.

**기능을 줄이려고 알림 전체를 비활성화했다(`0b52b59`).**
다시 켤 계획이 없으므로 착수하지 않는다.

다만 `@Scheduled` 에 인스턴스 락이 없어 서버를 늘리면 중복 실행되는 문제는
`AttachmentReservationCleaner` 에도 그대로 있다. **알림 없이도 다룰 수 있는 소재다.**

---

## 완료된 것

| | |
|---|---|
| **초과 트래픽 차단** | NGINX Leaky Bucket. 초과 99.6% 차단, CPU 100% → 10~25%, 통과 요청 p95 962 → 137ms |
| **PostgreSQL → MySQL 전환** | 수동 `CREATE INDEX` 로만 있던 인덱스 5개를 엔티티로 이관, 미사용 컬럼 6개 정리, 한글 이중 인코딩을 `HEX()` 로 검출 |

## 다루지 않을 것

| | 이유 |
|---|---|
| Kafka · MSA 도입 | 2 vCPU / 2GB 에 브로커가 들어가지 않고 해결할 문제도 없다 |
| 멀티 모듈 | 개발자 1명, 배포 단위 하나 |
| 구버전 업로드 경로 보강 | 앱이 presign 경로만 쓴다면 엔드포인트를 지우는 것이 답 |
| 폴더 존재 확인 check-then-act | `storageId` 가 외래키가 아니라 확인·저장 사이에 폴더가 지워지면 고아 행이 생긴다. 곁가지라 3번에 묶는다 |

---

## 작업할 때 지킬 것

### 1. 재현 테스트를 먼저 만든다

동시성은 말로만 아는 상태가 되기 쉽다. 격리수준 4개는 누구나 외운다.
**실제로 깨뜨려보고 숫자로 남긴 것**이 근거가 된다.

- ❌ "동시 요청 시 용량 제한이 초과될 수 있습니다"
- ✅ "동시 2 요청에서 15MB, 동시 10 요청에서 135MB 초과했고 그 이상은 커넥션 풀이 상한이었습니다"

부하는 **NGINX Rate Limiter 를 우회해 앱 포트로 직접** 보낸다.
`limit_req` 가 요청을 잘라내면 애플리케이션 레벨 측정이 안 된다.

JIT 워밍업 전에는 값이 2~3배로 나온다. **측정 전 200요청을 흘려보낸다.**

### 2. 대안은 논증이 아니라 실측으로 고른다

**채택하지 않을 것도 최소 하나는 구현해서 잰다.**

락 계열은 커넥션 점유가 핵심 비용이므로
`hikaricp_connections_active` / `pending`, `Innodb_row_lock_waits` / `_time` 을 함께 본다.

`performance_schema` 가 꺼져 있어도 `SHOW GLOBAL STATUS` 로 락 대기를 잴 수 있다.
누적값이므로 라운드 전후 증가분을 본다.

### 3. 실패한 시도와 한계를 기록한다

"이 방법을 써봤는데 기대만큼 개선되지 않았다", "여기까지는 되고 여기부터는 안 된다"는
서술이 신뢰를 만든다. 채택한 것만 적으면 처음부터 정답을 알았던 것처럼 보인다.

스토리지 항목의 `총 대기 ÷ (1+2+…+N-1)` 역산 실패가 그 예다.
통과 요청과 거부 요청의 락 점유 시간이 달라 전제가 틀렸다.

### 4. 측정값에는 환경을 함께 적는다

DB·인스턴스 사양·풀 크기가 바뀌면 값이 달라진다.
통합검색을 다시 재게 된 것도 PostgreSQL 기준 값이었기 때문이다.

### 5. 문서 형식

```
docs/problems/<주제>/
├── README.md        문제 정의와 판단
├── measurements.md  측정 원본
├── scripts/         재현·측정 스크립트
├── images/          그림
└── migration/       필요한 경우
```

글은 `문제 상황 → 고려한 대안과 탈락 이유 → 해결 과정 → 결과 → 한계` 순서로 쓴다.
