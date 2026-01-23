# 🧭 팀 운영 규칙 (Issue · Pull Request · Commit)

본 문서는 프로젝트에서 **Issue, Pull Request, Commit을 어떻게 운영하는지**에 대한
팀 공통 규칙을 정의합니다.

> 목적:
> - 작업 흐름을 명확히 하고
> - 이슈 상태 꼬임을 방지하며
> - GitHub Project를 Issue 중심으로 깔끔하게 유지하기 위함

## Issue 운영 규칙

### Issue 생성 시점
- 모든 작업은 **Issue 생성 후 시작**한다.
- 기능, 버그, 문서, 설정 작업 모두 Issue로 관리한다.

### Draft → Issue 전환
- Draft 상태로 작성된 항목은 **작업 시작 전에 반드시 Issue로 전환**한다.

### Issue 작성 규칙
- 제목은 “무엇을 하는지” 명확히 드러나야 한다.
    - 일정 API X
    - 메인 페이지 월별 일정 조회 API 구현 O
- Issue는 반드시 Project에 추가한다.


## Pull Request 운영 규칙

### 기본 원칙
- **1 Issue = 2 Pull Request**
- 하나의 PR에 여러 커밋은 허용
- 한 개의 issue에서 feature/#{PR번호}  → dev  → main으로 가기에 2개의 pr


상세 규칙
- 이슈 자동 종료는 PR에서만
- main merge 시 issue 자동 Done
- 단 feature/#{PR번호} 브랜치 → dev갈 때는 Closes: #{이슈번호}를 쓰지 말고 dev → main 브랜치 갈 때만 진행한다.
  - feature/#{PR번호} 브랜치 → dev 갈땐 Refs: #{이슈번호}를 사용한다.

PR Description 필수 템플릿
- dev에서 main으로
```text
## 변경 내용
- 커밋 컨벤션 문서 분리
- README에서 링크 연결

Closes: #4
```

- feature/#{PR번호}에서 dev
```text
## 변경 내용
- 커밋 컨벤션 문서 분리
- README에서 링크 연결

Refs: #4
```

## commit 
- commit convention은 다음 문서에서 확인바랍니다.

[Commit Convention 문서 바로가기](./docs/convention/commitconvention.md)




