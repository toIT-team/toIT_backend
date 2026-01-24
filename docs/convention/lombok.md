프로젝트 초기에서 우려해야할 상황이 생겼다.

1. 반복되는 코드 증가
- 생성자, 로거 선언, 단순한 getter 코드가 클래스마다 반복
- 실제 비즈니스 로직보다 의미 없는 코드가 많이 생김

2. 코드 스타일의 비일관성
- 백엔드는 현재 2명이 있는데 2명 다 생성자 작성 방식이 달랐다. 각자 스타일이 달라 코드의 일관성이 지켜지지 않는다라는 것
    - 방식은 @Autowired를 쓰는 방식 or @Autowired를 쓰지 않고 기본 생성자 작성

따라서 Lombok이라는 라이브러리를 통해 다음과 같은 원칙을 도입하기로 하였다.

1. Lombok은 편의성 도구가 아니라 규칙 강제 도구로 사용한다.
2. 모든 어노테이션을 허용하지 않고, 의도가 명확한 어노테이션만 선별한다.

## 선별된 애노테이션
| **어노테이션** | **추천 대상** | **설명 및 권장 사항** |
| --- | --- | --- |
| **@Getter** | 클래스 전체 | 모든 필드에 대한 Getter 생성. 필드 레벨보다는 클래스 레벨 사용을 권장. |
| **@RequiredArgsConstructor** | Service, Controller | `final`이 선언된 필드만 모아 생성자를 만듦. **스프링 생성자 주입** 시 필수. |
| **@Slf4j** | 모든 클래스 | 로그를 남기기 위한 `log` 객체 자동 생성 (SLF4J). |
| **@NoArgsConstructor** | access = AccessLevel.PROTECTED | JPA Entity 필수. 무분별한 `new` 객체 생성을 방지함. |


1. Getter
- 모든 클래스에서 필드의 값을 가져오기 위해 Getter 어노테이션을 선별함.
2. RequiredArgsConstructor
- 생성자 주입 부분에서 Autowired 어노테이션을 쓰지 않기로 결정한 부분에서 생성자를 다 작성해야하지만 이 어노테이션을 통해 간편하게 작성할 수 있도록 하여 선별함
3. Slf4j
- 클래스마다 공통된 로거(Logger) 객체를 자동으로 생성해주는 어노테이션이다. Logger를 설정할 때 팀의 2명이 각각 Logger 객체를 직접 생성해서 사용하거나 기본 입출력을 사용해서 개발을 했다. 이것도 이 어노테이션을 통해 공통화가 필요해서 이렇게 선별을 진행
4. NoArgsConstructor
- 우리는 스프링 DATA JPA를 사용한다. JPA의 요구사항은 “엔티티 클래스는 public 또는 protected 기본 생성자를 가져야 한다.”라고 명시되어 있다.
  - 왜냐하면 private일 경우 JPA가 접근을 하지 못하기 때문이다. 그래서 엔티티 생성자를 public or protected로 선언해야되는데 public으로 사용하면 필수 값 없이 외부에서 엔티티 생성이 가능하다. 그래서 protected를 사용해서 생성자를 만들어야함
  - protected 생성자를 사용하는 것이 코드의 복잡성을 올려 → NoArgsConstructor(access = AccessLevel.PROTECTED)로 형태로 진행
  - 또한 이렇게 진행하게 되면 외부에서 무분별한 new 차단이 가능

### 권장되는 애노테이션 사용 예시
#### @Getter 예시 
엔티티에 적용
```java
@Getter
public class Member {
    private Long id;
    private String name;
    private int age;
}
```

#### @RequiredArgsConstructor 예시
표현 계층
```java
@RequiredArgsConstructor
public class MemberController {
	private final MemberRepository memberRepository;
}
```
비즈니스 계층에 적용
```java
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
}
```

#### @NoArgsConstructor 예시
Entity
```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {
    private String name;
}
```
dto → dto는 도메인 규칙을 가지지 않아서 protected 할 필요 없음
```java
@NoArgsConstructor
@Getter
public class MemberRequest {
    private String name;
}
```

## 선별되지 않는 애노테이션
| **애노테이션** | **권장 등급** | **지양하는 이유 (Role & Risks)** | **대체 방법** |
| --- | --- | --- | --- |
| **@Data** | **절대 지양 (Entity)** | `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode` 등을 모두 포함하는 '맥시멀리스트' 애노테이션. Entity에서 사용 시 **양방향 참조로 인한 무한 루프**나 **의도치 않은 데이터 변경**을 유발 | `@Getter`, `@NoArgsConstructor` 등 필요한 것만 개별 선언 |
| **@Setter** | **지양 (Entity)** | 객체의 상태가 어디서든 변경될 수 있어 **객체의 일관성을 유지하기 어려움.** 데이터 수정의 의도를 파악하기 힘들어진다. | 의미 있는 이름의 **비즈니스 메서드**를 작성 (예: `updateAddress()`, `cancelOrder()`) |
| **@AllArgsConstructor** | 지양 | 클래스 레벨에 선언하면 **필드 선언 순서에 따라 생성자의 파라미터 순서가 결정된다**. 개발자가 필드 순서를 바꿨을 때, 컴파일 에러 없이 잘못된 값이 들어갈 수 있어 매우 위험 | `@Builder`를 사용하거나, 필요한 필드만 포함하는 생성자를 직접 작성 |
| **@EqualsAndHashCode** | 지양 | 기본적으로 모든 필드를 비교 대상으로 삼는다. JPA에서는 가변 필드나 지연 로딩 필드가 포함될 경우 **성능 저하**나 **동일성 보장 문제**가 발생할 수 있음 | 필요한 경우에만 식별자(`@Id`) 필드를 기준으로 직접 구현하거나 `of` 옵션 사용 |
| **@Builder** | Entity에는 지양 | Entity에 @Builder를 붙이면 이런 문제가 생기기 쉬움 | 빌더 패턴 구현. 가독성 높은 객체 생성을 위해 사용. |


1. Data
- 이 어노테이션을 사용할 시 엔티티에서 제일 위험한 것들을 한 번에 만들어버린다.
- 양방향 참조로 인한 무한 루프를 보면 만약 team, members가 있을 때 Data 어노테이션이 만든 toString으로 출력을 한다면 toString의 내부 동작으로 인해 team.toString → members 출력, Member.toString() → team 출력하는 무한 반복이 생성됨 → StackOverFlowError가 발생함.

2Setter
- 객체의 상태가 어디서든 변경된다라는 것은 Setter 어노테이션을 넣는다면 Service, Controller, Test, 심지어 다른 도메인 로직안에서도 값이 변경될 수 있다. 즉, 이 객체가 언제, 왜, 어떤 규칙으로 바뀌었는지 추적이 불가하다.
  - 다음과 같은 코드는 규칙 검사 없고, 로그 남기기가 어렵다.
    ```java
    member.setStatus(MemberStatus.BLOCKED);
    ```

  - 다음 코드를 한다면 의도를 드러내는 메소드 사용을 통해 → 회원을 차단하는 행위를 확실하게 알 수 있다. 즉, 데이터 수정의 의도를 파악할 수 있음.

    ```java
    public void block() {
        if (this.status == MemberStatus.DELETED) {
            throw new IllegalStateException();
        }
        this.status = MemberStatus.BLOCKED;
    }
    member.block();
    ```

2. AllArgsConstructor
- 이 어노테이션은 모든 필드를 선언된 순서 그대로 파라미터로 받는 생성자를 만든다. 그런데 이게 필드 순서를 바꾸면 생성자도 자동으로 바뀐다. 바뀐다고 하였을 때 다음과 같은 코드는 컴파일 에러 없이 잘못된 값이 들어간다.

```java
User user = new User("jaeyoon", 25, "test@test.com");
```

3. EqualsAndHashCode
- 기본 동작부터 보면 모든 필드를 비교 대상으로 equals()와 hashCode()를 생성함. 만약 이 어노테이션을 쓰면 equals() → name 비교, hashCode() → name을 기준으로 계산
- 이 어노테이션을 쓰면 아무 옵션 없이 모든 필드를 비교 대상으로 삼을 수 있다. 그런데 JPA에서 지연 로딩 필드가 포함될 경우 equals를 호출하였을 때 프록시가 실제 엔티티가 필요하다고 판단해서 DB 조회가 발생함 즉, equals 한 번 했는데 쿼리가 날라감

```java
member1.equals(member2); // equals 한 번 호출
```

즉, 단순 비교 연산이 DB I/O를 발생시킬 수 있음.

- 가변 필드가 포함될 경우 동일성 보장 문제가 생긴다.
  - equals/hashCode의 핵심 규칙은 객체가 Set/Map에 들어간 후에는 equals/hashCode 결과가 변하면 안된다라는 핵심 규칙이 있다.
  - 그런데 엔티티는 가변 객체이다. @EqualsAndHashCode가 name을 포함하고 있으면 → hashCode 값 변경, equals 결과 변경을 한다.
- JPA 엔티티는 가변 객체임
```java
@EqualsAndHashCode
class Member {
    private String name; // 가변 필드
}
```

→ 이 경우 name이 변경되면 → hashCode() 값 변경 → equals() 값 변경이 일어난다.

실제로 발생하는 문제 사례를 보자
```java
Set<Member> set = new HashSet<>();
set.add(member);

member.setName("newName");

set.contains(member); // false
```

- 내부 동작 과정
  - 1. 객체 저장시(add)
    ```java
    member.name = "oldName";
    hashCode = hash("oldName"); // 예: 123
    ```
  - hashCode() 호출 → 해시 값으로 버킷 결정 → bucket[123]에 객체 저장
  - 2. 객체 상태 변경
    ```java
    member.setName("newName");
    hashCode = hash("newName"); // 예: 999
    ```
  - 3. 객체 조회 시
    ```java
    member.hashCode(); // 999
    ```

  - bucket[999]에서 탐색 → 실제 객체는 bucket[123]에 존재 → 탐색 실패 → false 반환

즉, Lombok이 다음과 같은 코드를 만들어주는데 name 값이 바뀌었기 때문에 계산 결과가 바뀌는 것

```java
@Override
public int hashCode() {
    return Objects.hash(name);
}
```

5. Builder
- 만약 엔티티에서 Builder를 붙이면 다음과 같은 문제가 발생하기 쉬움
  - 생성 규칙이 흐려짐 → 어떤 필드는 필수 같은 도메인 규칙이 빌더로 우회될 수 있음
  - 부분 초기화된 엔티티가 생기기 쉬움 → builder()는 필요한 값만 넣고도 build()가 되니 필수값 누락 위험 커짐
  - 연관관계/컬렉션 필드가 위험 → @OneToMany List<> 같은 건 초기화, 편의메서드 동기화가 중요한데, builder로 만들면 누락/불일치가 발생하기 쉬움
- 그래서 Entity는 정적 팩토리 메소드로 의도가 드러나는 생성을 권장함 결론은 엔티티에 대한 규칙이 있어야 쓸 수 있음

## 결론
우리는 lombok 라이브러리를 통해 규칙 강제 도구로 사용하며 그 중 @Getter, @RequiredArgsConstructor, @Slf4j, @NoArgsConstructor 어노테이션만 사용