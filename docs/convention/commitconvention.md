## 📝 Commit Convention
이 프로젝트는 **Conventional Commits** 규칙을 기반으로 커밋 메시지를 작성합니다.

### 기본 포맷
- **type은 필수**
- **scope는 선택** (모듈/도메인이 명확한 경우 사용 권장)
- **description**
    - 짧고 명확하게 작성
    - 명령형 현재 시제 사용
    - 마침표(`.`) 사용 X
- **Breaking Change / 이슈 연결 / 중요한 설명**은 footer에 작성
```
[optional scope][optional !]: 

[optional body]

[optional footer(s)]
```

## Commit Type 목록
 
| type | 설명 |
|------|------|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 변경 |
| refactor | 기능 변경 없는 구조 개선 |
| perf | 성능 개선 |
| test | 테스트 추가/수정 |
| build | 빌드/의존성 변경 |
| ci | CI 설정 변경 |
| chore | 기타 작업(설정, 정리 등) |
| style | 포맷팅, 세미콜론 등 의미 없는 변경 |

### 예시
- Breadking Change == 기존 사용자가 코드를 수정하지 않고 업그레이드
```
feat(auth): add kakao login
fix(schedule): handle timezone conversion
refactor(api): split controller and service
docs(readme): add commit convention section
feat(api)!: rename /links endpoint to /item
```

## 🔗 이슈 / 티켓 연결 (Footer)
Git trailer 형식을 사용합니다.

- 커밋 메시지(Footer)에 이슈 번호를 적으면 Github이 자동으로 이슈와 커밋을 연결
- 커밋에서는 Closes를 하지 않습니다. → PR에서만 사용합니다.
```
type(scope): description
- [body]
Refs: #123
```
