## 🚀 Release & Version Management

이 프로젝트는 `main` 브랜치를 기준으로 배포 가능한 버전을 관리하며, GitHub Releases를 사용하여 배포 이력과 버전별 변경 사항을 기록합니다.

### 🏷 Versioning Rule

배포 버전은 Semantic Versioning 형식을 따릅니다.

```text
vMAJOR.MINOR.PATCH
```

예시는 다음과 같습니다.

```text
v1.0.0  최초 배포
v1.0.1  버그 수정
v1.1.0  기능 추가
v2.0.0  큰 구조 변경 또는 호환되지 않는 변경
```

### 🌿 Release Branch Policy

현재 프로젝트는 별도의 `release` 브랜치를 사용하지 않습니다.

```text
feature/*
   ↓ Pull Request
main
   ↓ Tag
GitHub Releases
```

`main` 브랜치는 항상 배포 가능한 상태를 유지하는 것을 원칙으로 하며, 실제 배포 시점은 Git tag와 GitHub Releases를 통해 기록합니다.

### 📦 GitHub Releases

GitHub Releases에는 다음 내용을 기록합니다.

- 배포 버전
- 주요 변경 사항
- 추가된 기능
- 수정된 문제
- 인프라 변경 사항
- 배포 대상 브랜치 및 태그

Release 예시는 다음과 같습니다.

```text
Tag: v1.0.0
Target: main
Release title: v1.0.0 - 첫 배포
```

### 📝 Release Notes 작성 기준

Release Notes는 다음 형식을 기준으로 작성합니다.

```md
## v1.0.0 - 첫 배포

### 주요 내용
- toIT 백엔드 첫 운영 배포
- 인증, 사용자, 보관함, 자료 관리 기능 반영
- 일정, 알림, 피드백, 관리자 기능 반영

### 인프라
- AWS Lightsail 서버 배포
- PostgreSQL 데이터베이스 사용
- NGINX Reverse Proxy 적용
- Let's Encrypt SSL 인증서 적용
- 테스트 서버 구축

### 배포 대상
- Branch: `main`
- Tag: `v1.0.0`
```

### ⚠️ Release Asset Policy

현재 저장소가 Public Repository일 수 있으므로, GitHub Releases에는 실행 가능한 `.jar` 파일을 첨부하지 않습니다.

Release는 배포 이력과 변경 사항을 기록하는 용도로 사용하며, 실제 서버 배포 파일은 로컬 또는 CI/CD 환경에서 빌드하여 서버에 반영합니다.

### 🔄 Future CI/CD Plan

추후 CI/CD가 도입되면 GitHub Release 발행을 배포 트리거로 사용할 수 있습니다.

```text
GitHub Release Publish
        ↓
GitHub Actions 실행
        ↓
Spring Boot JAR 빌드
        ↓
Lightsail 서버로 전송
        ↓
애플리케이션 재시작
```

이를 통해 `main`에 merge되는 시점과 실제 운영 배포 시점을 분리하고, 명확한 배포 버전과 변경 이력을 유지합니다.