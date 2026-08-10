# Apple 로그인만 이름이 `apple_000379.8` 로 저장된다 — 원인 추적기

> 카카오로 가입한 유저는 이름이 멀쩡한데, Apple로 로그인한 유저만 DB에 `apple_000379.8`
> 같은 이상한 이름이 저장되고 있었습니다. **왜 Apple만 그런지**를 데이터에서 코드까지
> 거꾸로 추적한 기록입니다.

# 1. 증상 — DB에서 발견한 이상한 이름

사용자 테이블을 보다가 눈에 걸린 게 있었습니다. 카카오 유저는 이름이 정상인데, Apple 유저만
이름 자리가 이상했습니다.

| image_url | name | provider_id |
| --- | --- | --- |
| http://k.kakaocdn.net/.../img_640x640.jpg | 김민지 | 4888676490 |
| http://k.kakaocdn.net/.../img_640x640.jpg | 온유 | 4889371681 |
| *(없음)* | **apple_000379.8** | 000379.85ca06b3b14f4113bb8b889ee1eed78a.1111 |
| http://k.kakaocdn.net/.../img_640x640.jpg | 김지우 | 4891041344 |
| *(없음)* | **apple_001992.c** | 001992.c248bfe7e08c4e8e9a106a83240256fd.1525 |

카카오 유저(`김민지`, `온유`, `김지우`…)는 실명이 들어와 있는데, **Apple 유저만
`apple_000379.8`, `apple_001992.c` 처럼 알 수 없는 문자열**이었습니다. 프로필 이미지도
Apple 쪽은 비어 있었습니다.

# 2. `apple_000379.8` 은 어디서 나온 값인가

이 문자열의 출처부터 찾았습니다. Apple 로그인을 처리하는 서비스에 정확히 이 형태를 만드는
코드가 있었습니다.

```java
// AppleAuthMemberService.saveOrUpdate()
String providerId = userInfo.getProviderId();
String fallbackName = "apple_" + providerId.substring(0, Math.min(8, providerId.length()));
String resolvedName = hasText(userInfo.getName()) ? userInfo.getName() : fallbackName;
```

`providerId` 가 `000379.85ca06b3b14f4113bb8b889ee1eed78a.1111` 이니, 앞 8글자를 자르면
`000379.8` 이고 `apple_` 를 붙이면 정확히 **`apple_000379.8`**. DB 값과 완벽히 일치했습니다.

즉 이건 랜덤 버그가 아니라, **의도적으로 만들어 둔 fallback(대체) 이름**이었습니다. 문제는
"왜 매번 fallback 으로 빠지느냐"였습니다. 조건은 하나뿐입니다.

```java
String resolvedName = hasText(userInfo.getName()) ? userInfo.getName() : fallbackName;
//                    userInfo.getName() 이 비어있으면(null/blank) → fallbackName
```

**`userInfo.getName()` 이 항상 비어있다**는 뜻이었습니다.

# 3. 왜 이름이 항상 비어있나 — Apple의 특성

`getName()` 이 어디서 값을 읽는지 따라가 봤습니다.

```java
// AppleUserInfo
public String getName() {
    return oidcUser.getFullName();   // ID Token(JWT)의 name claim을 읽음
}
```

`oidcUser.getFullName()` 은 **ID Token(JWT) 안의 표준 claim**에서 이름을 꺼냅니다. 그런데
여기에 함정이 있었습니다.

> **Apple은 ID Token 안에 이름(name)을 절대 넣어주지 않습니다.**

Apple의 id_token claim에는 `sub`(고유 ID), `email`, `email_verified` 정도만 들어있고 **이름
필드 자체가 없습니다.** 그래서 `getFullName()` 은 언제나 `null` 이고, 코드는 항상 fallback
으로 빠집니다. → `apple_xxxxxxxx`.

카카오와 비교하면 차이가 분명합니다.

| | 카카오 | **Apple** |
| --- | --- | --- |
| 이름 전달 방식 | userinfo 응답에 **매번** 포함 | **최초 가입 1회만** 제공 |
| 이름이 담기는 위치 | userinfo JSON | **ID Token이 아니라, 콜백 POST의 별도 `user` 파라미터** |
| 재로그인 시 | 매번 이름 옴 | **다시는 안 옴** |

핵심은 두 가지입니다.

1. **Apple 이름은 ID Token에 없다.** 최초 동의 직후 **콜백 요청 body의 `user` 라는 별도
   파라미터**로 딱 한 번 옵니다.
   ```
   POST /login/oauth2/code/apple
     ...&user={"name":{"firstName":"길동","lastName":"홍"},"email":"..."}
   ```
2. 이 `user` 는 **표준 OIDC claim이 아니라 Apple만의 확장 필드**라서, Spring Security의 기본
   OIDC 흐름(`OidcUserService`)이 파싱해주지 않습니다. 지금 코드는 이 `user` 파라미터를 읽는
   로직이 아예 없습니다.

# 4. 그래서 문제는 두 겹이었다

정리하면 이렇게 됩니다.

1. **최초 로그인**: Apple이 이름을 `user` POST 파라미터로 보내주는데, 코드는 ID
   Token(`getFullName()`)만 보고 있어서 **놓칩니다.** → fallback 저장.
2. **재로그인**: Apple은 이름을 **두 번 다시 보내주지 않습니다.** → 최초에 못 잡으면 **영영 못
   잡습니다.**

여기에 한 가지 함정이 더 있습니다. 이미 가입된 유저를 갱신하는 쪽은 이렇게 되어 있습니다.

```java
.map(entity -> {
    // Apple은 최초 로그인 때만 이름을 전달하므로 실제 이름이 있을 때만 업데이트
    entity.updateSocialProfile(userInfo.getEmail(), userInfo.getName(), null);
    return entity;
})
```

`userInfo.getName()` 이 항상 `null` 이니 이름은 **한 번 fallback으로 박히면 이후 로그인으로도
교정되지 않습니다.** 카카오도 마찬가지로 재로그인 시 이름을 덮어쓰지 않도록 되어 있어, 최초에
잘못 들어간 값은 자동으로 고쳐지지 않습니다.

# 5. 어떻게 풀 것인가 — 선택지 비교

| 방안 | 내용 | 한계 |
| --- | --- | --- |
| **A. `user` 파라미터 파싱** | 최초 콜백의 `user` JSON에서 이름을 꺼내 저장 | 구현 복잡, **기존 유저는 소급 복구 불가** |
| **B. 앱에서 직접 입력** | 소셜 이름에 의존하지 않고 온보딩에서 닉네임을 받음 | 온보딩 흐름 추가 필요 |
| C. fallback 문구 개선 | `apple_xxx` 대신 "사용자N" 등으로 | 근본 해결 아님(임시 완화) |

**A안 — `user` 파라미터 파싱 (근본이지만 최초 1회 한정)**
Apple이 이름을 주는 유일한 순간인 최초 콜백에서 `user` 폼 파라미터를 직접 파싱해야 합니다.
`OidcUserService` 는 원본 HTTP 요청 접근이 까다로워, 보통 `RequestContextHolder` 로 현재 요청의
`user` 파라미터를 읽거나 토큰 응답 단계에서 가로채는 방식으로 구현합니다. 단, **이미 fallback
으로 저장된 기존 유저는 Apple이 이름을 재전송하지 않으므로 되살릴 수 없습니다.**

**B안 — 앱 온보딩에서 닉네임 입력 (권장)**
사실 이 서비스에는 이미 추가 정보 입력 플로우(`NEEDS_ADDITIONAL_INFO` /
`requiresAdditionalInfo()`)가 있습니다. **"Apple은 이름을 못 받는다"를 전제로 두고**, 신규 가입
온보딩에서 닉네임을 받도록 유도하는 것이 가장 견고합니다. Apple 로그인 가이드도 앱이 이름을
자체적으로 관리하도록 권장합니다.

# 6. 회고

- **`apple_000379.8` 은 버그가 아니라 fallback이 그대로 노출된 결과**였습니다. 진짜 원인은
  "Apple은 이름을 ID Token이 아니라 최초 콜백 `user` 파라미터로 딱 한 번만 준다"는 특성을
  코드가 반영하지 못한 것이었습니다.
- 교훈은 **소셜 로그인마다 이름/이메일을 주는 방식이 완전히 다르다**는 점입니다. 카카오 기준으로
  짠 로직을 Apple에 그대로 적용하면, ID Token만 바라보다 이름을 통째로 놓칩니다.
- 방향은 **Apple은 이름을 신뢰하지 않는다는 전제(B안)를 기본으로 삼고**, 최초 가입 시 앱에서
  닉네임을 받도록 온보딩을 정비하는 것입니다. `user` 파라미터 파싱(A안)은 최초 1회만 잡을 수
  있고 기존 유저는 복구하지 못하므로, 보조 수단으로만 고려합니다.
- 이미 fallback 이름으로 저장된 기존 Apple 유저들은 **한 번은 닉네임 재설정을 유도**하는
  마이그레이션이 필요합니다.
