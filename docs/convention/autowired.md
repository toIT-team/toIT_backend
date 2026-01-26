같이 협업을 하는 백엔드 팀원끼리 생성자를 주입하는 방식이 달랐다. @Autowired를 사용해서 하는 방식을사용하거나 사용하지 않는 형태로 바뀌어 이 이노테이션을 사용할지를 고민하다가 유용한 선택을 하기 위해 @Autowired가 어떤 기능을 하고 어떤 이점이 있는지 분석하게 되었다.

Spring boot에서 생성자 주입을 위해 @Autowired를 사용할 수 있다. 우선, Spring 애플리케이션 실제 동작순서를 보자

## Spring 애플리케이션 실제 동작 순서
보통 다음과 같은 한 줄로 실행
```java
SpringApplication.run(ToitApplication.class, args);
```

이 한줄을 실행하면 생성 → 빈 등록 → 빈 생성 → 주입이 전부 일어남


전체 큰 흐름
1. JVM 실행
    1. JVM이 main 메소드 실행 → 이게 저 위에 있는 run == 여기까지 순수 자바
2. SpringApplication 생성
```java
new SpringApplication(Application.class);
```
- 이 시점에서 스프링은 내부적으로 이런 정보들을 준비
    - 이게 웹 애플리케이션인지(Servlet/Reactive)
    - 어떤 ApllicationContext 구현체를 쓸지
    - 어떤 클래스가 시작 클래스인지
- 이때 컨테이너는 생성 안 됨 → 단지 컨테이너를 만들 준비만함

3. 스프링 컨테이너(ApplicationContext) 생성
   1. 이 순간 스프링 전용 객체 공장이 생긴다.
   2. 앞으로 모든 빈은 이 컨테이너가 관리
   3. 이 시점부터 new 하는 대신 컨테이너가 객체를 관리

4. 빈 정의(BeanDefinition) 수집
    1. 빈을 바로 만들지 않고 → 설계도부터 만든다. 스프링은 “일단 어떤 객체들을 만들어야할지 목록 정리해보자
        1. 빈 정의
            1. 이 클래스를 빈으로 만들겠다, 이 클래스는 싱글톤이다. 생성자는 이거다. == 객체가 아니라 메타 정보
        2. 빈 정의 모으는 방법
            1. @SpringBootApplication → 내부 @ComponentScan 있음 @Service, @Controller 같은 걸 다 훑음 → 이것들을 빈 후보로 만들고 → @Configuration + @Bean 이걸로 빈 정의로 등록
            2. 밑 코드 → ObjectMapper라는 Bean을 만들 거다. 아직 new 안 함
            ```java
           @Configuration
           class AppConfig {
    
           @Bean
           public ObjectMapper objectMapper() {
           return new ObjectMapper();
             }
           }
            ```
    2. 이 상태는 컨테이너 있고, 빈 정의 있고, 실제 객체는 아직 없다.
5. 빈 생성
   1. 이제 스프링은 “이제 실제 객체를 만들자”함
   2. OrderService 생성 시작
   3. OrderService를 만들려는데 스프링이 멈춰서 생각 → “어 생성자에 MemberRepository가 필요하네” 그래서 컨테이너한테 “MemberRepository 타입 빈 있나” 라고 물어봄 있으면 이 빈 생성 없으면 에러
6. 의존성 주입
   1. MemberRepository 생성 → OrderService 생성자 호출 → memberRepository를 파라미터로 전달 == new OrderService(memberRepository) 효과 발생
7. 초기화 콜백
   1. 객체 생성 + 주입이 끝나면 초기화 콜백 @PostConstruct, @InitializinBean @init-method 같은 초기화 콜백 실행 == 이제 객체는 완전한 상태
8. 애플리케이션 실행 완료

## 스프링 컨테이너란?
- 애플리케이션에서 사용하는 객체(Bean)를 대신 생성하고 필요한 객체들끼리 연결해주고 언제 만들고 언제 없앨지까지 관리하는 중앙 관리자

→ new를 몰아서 대신 해주는 객체 공장 + 관리자

이게 왜 필요했냐 원래 자바에서는
```java
A a = new A();
B b = new B(a);
C c = new C(a, b);
```
- 객체가 많아질 수록 new 지옥, 누가 누구를 만드는지 뒤엉킴, 교체/테스트/확장 어려움

→ 객체 생성과 사용 로직이 뒤섞임

스프링은 이걸 다음과 같이 나눔
- 객체 생성 & 관리 → 컨테이너
- 객체 사용 → 개발자 코드

스프링 컨테이너의 정체를 기술적으로 본다면 == ApplicationContext 인터페이스의 구현체

컨테이너가 실제로 하는 일(핵심 5가지)

1. 객체(빈) 생성
2. 객체 사이 연결(의존성 주입)
3. 객체 생명주기 관리
4. 싱글톤 관리()
5. 설정 기반 객체 조립


컨테이너는 다음과 같음 코드
```java
OrderService service = applicationContext.getBean(OrderService.class);
```

> 스프링 컨테이너는 애플리케이션에서 쓰는 모든 객체를 대신 만들고, 서로 필요한 객체를 연결하고, 생성부터 종료까지 책임지는 “객체 관리자”다.

## Bean이란?
- 스프링 컨테이너가 생성하고, 보관하고, 관리하는 객체

그냥 자바 객체는
```java
Member member = new Member();
```

- JVM이 생성, 개발자가 직접 new, 생명주기 관리 x,  아무도 대신 관리해주지 않음

스프링 Bean
```java
@Service
public class OrderService { }
```

- 스프링 컨테이너가 생성, 컨테이너 내부에 등록됨, 생명주기 관리 O, 다른 Bean들과 연결 가능

== 컨테이너의 관리 대상 객체

## 그러면 스프링 컨테이너랑 Bean 관계가 어떻게 되는 걸까?

컨테이터는 관리자, Bean은 관리 대상

컨테이너 = 물류센터, Bean = 물류센터에 보관된 물건들

물류센터 → 물건을 직접 쓰지 않음 대신 보관, 관리, 배치, 출고 책임

물건 → 물류센터가 없으면 그냥 개별 물건, 물류센터에 들어가면 관리 대상

즉,

**@Controller, @Service, @Repository가 붙은 “객체”가 컨테이너에 들어가면, 그 객체를 “Bean”이라고 부른다.**


실행하는 과정을 보자

1. 요청이 오면(HTTP 요청) → DispatcherServlet → OrderController Bean → OrderService Bean → OrderRepository Bean
- 새 객체 안 만들고 컨테이너에 있던 Bean 그대로 사용
> @Controller로 등록된 클래스의 객체 자체가 Bean이고 그 Bean을 컨테이너가 보관 및 관리하며 사용한다

@Controller Bean을 실행 시키는 놈은 스프링 컨테이너가 아니라 DispatcherServlet임.

<br/>
DispatcherServlet → 모든 HTTP 요청을 제일 먼저 받는 “프론트 컨트롤러” == 스프링 MVC의 입구

> 요청이 오면 DispatcherServlet이 “스프링 컨테이너에 이미 등록 및 생성되어 있는 Controller Bean을 찾아서 그 Bean의 메서드를 호출(실행)한다.”

### 빈 생성 단계에서 의존관계를 본다
예를 들어

```java
@Service
class OrderService {

    private final MemberRepository memberRepository;

    public OrderService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

이 시점에서 스프링이 하는 생각

1. 스프링 컨테이너(ApplicationContext)가 OrderService Bean 생성을 시도한다.
2. 내부적으로 BeanFactory가 OrderService의 생성자를 분석한다.
    - @Autowired가 붙은 생성자가 있는지 확인
    - 없다면, 생성자 개수 확인
    - → “이 생성자를 주입 생성자로 사용” 결정
3. 주입 생성자의 파라미터를 확인한다.
    - MemberRepository 타입이 필요함을 인지
4. BeanFactory가 ApplicationContext 내부에 등록된 BeanDefinitionRegistry에서 MemberRepository BeanDefinition을 탐색한다. (※ ApplicationContext → BeanFactory 위임)
5. MemberRepository Bean이 아직 생성되지 않았다면 → 해당 Bean을 먼저 생성한다.
6. 생성된 MemberRepository 인스턴스를 OrderService 생성자의 파라미터로 전달한다. (이 단계가 autowiring, 즉 자동 주입)
7. OrderService 생성자가 실행되며, 전달받은 MemberRepository로 final 필드가 초기화된다.
8. OrderService Bean 생성 완료 → 이제 완전한 상태의 Bean이 컨테이너에 등록된다.

### @Autowired는 2번 단계에서 해석되고 6번 단계에서 효과가 나타남
개발자가 쓴 코드

```java
@Service
public class OrderService {

    private final MemberRepository memberRepository;

    @Autowired
    public OrderService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```
생성자 분석 단계 → @Autowired가 붙은 생성자가 있는지 확인

- 여기서 스프링은 딱 이 질문함 → 개발자가 @Autowired로 지정한 생성자가 있나?
    - 있으면 → 그 생성자를 주입 생성자로 확정
    - 없으면 → 생성자 개수 규칙(1개면 자동 선택)

즉, @Autowired는 “이 생성자를 써라”라는 명시적 힌트 → 이 힌트는 2번 단계에서만 사용됨

이제 6번 단계에서 @Autowired의 효과가 실제로 나타나는 지점

- 자동 주입 실행 단계 → 생성된 MemberRepository 인스턴스를 OrderService 생성자의 파라미터로 전달한다. (이 단계가 autowiring, 즉 자동 주입)

→ new OrderService(memberRepository); 이 주입 행위 자체를 통상 == autowiring 이라고 부름

→ 생성자에 @Autowired가 있었든, 없었든 2번에서 선택된 생성자를 기준으로 실행

### 생성자 주입 방법

4가지로 나뉨

1. 생성자에 @Autowired 명시
    1. 주입 생성자를 명시적으로 지정
    2. 생성자가 여러 개일 때 필요
    3. 모든 스프링 버전에서 동작
2. @Autowired 생략 → 생성자 1개
3. Lombok을 이용한 생성자 주입

```java
@RequiredArgsConstructor
@Service
public class OrderService {
private final MemberRepository memberRepository;
}
```

4. @Configuration + @Bean 에서 생성자 주입
```java
@Configuration
public class AppConfig {

    @Bean
    public OrderService orderService(MemberRepository memberRepository) {
        return new OrderService(memberRepository);
    }
}
```

### @Autowired를 사용해야할 상황
1. 생성자가 여러 개인 경우
- 스프링이 어느 생성자를 주입용으로 써야하는지 판단 불가
2. 필드 주입을 사용하는 경우 == 비권장하지만 존재
```java
@Autowired
private MemberRepository memberRepository;
```

- 필드는 생성자처럼 “주입용”이라는 힌트가 없음, 어노테이션 없으면 그냥 일반 필드

→ @Autowired 없으면 **주입 자체가 안 됨**

3. Setter 주입(선택적 의존성) == 수정자 주입
```java
@Autowired
public void setMemberRepository(MemberRepository memberRepository) {
this.memberRepository = memberRepository;
}
```

- 필수가 아닌 의존성, 테스트나 설정에 따라 바뀔 수 있는 경우

→ 이 메소드가 주입용 메소드라는 표시가 필요

4. 선택적 주입
```java
@Autowired(required = false)
private OptionalBean optionalBean;

or

@Autowired
public OrderService(Optional<DiscountPolicy> discountPolicy) {}
```
- Bean이 없을 수도 있는 상황
- 생성자 단일 규칙으로는 표현하기 어려움

## @Authowired란?
- 스프링 컨테이너가 자동으로 빈을 주입해주는 어노테이션
```java
@Service
public class OrderService {

    @Autowired
    private MemberRepository memberRepository;
}
```
→ 스프링이 런타임에 리플렉션에 주입

→ 코드가 짧고 직관적

→ 단점 : 테스트 어려움, 불변성 보장 불가

### ## 그러면 @Autowired가 하는 일?
```java
@Service
public class OrderService {

    @Autowired
    private MemberRepository memberRepository;
}
```
- 스프링에게 다음과 같이 전달 → “OrderService를 만들 때 MemberRepository 타입 Bean을 찾아서 여기에 주입해줘”

즉, 주입 대상 위치를 알려줌, 주입 기준(타입)을 알려줌

이게 왜없으면 필드 주입에서

```java
@Service
public class OrderService {

    private MemberRepository memberRepository;
}
```

- 스프링은 이 필드를 그냥 일반 필드로 취급, 아무것도 주입 안함 → 실행시 NullPointerException이 뜸
- 컨테이너는 여기다 넣어야한다라는 걸 모름

그러면 생성자 주입에서 @Autowired가 없어도 될까?

```java
@Service
public class OrderService {

    private final MemberRepository memberRepository;

    public OrderService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

- 이 경우 스프링은
    - 이 클래스는 생성자가 하나고 생성자 파라미터가 Bean 타입이네? 그럼 이걸 주입하라는 뜻이구나

아직 작업하는 부분에서 @Autowired를 사용해야할 상황이 없어서 굳이 사용안 해도 될 것 같다.