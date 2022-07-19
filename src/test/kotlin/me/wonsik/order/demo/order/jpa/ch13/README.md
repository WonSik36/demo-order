# 웹 애플리케이션과 영속성 관리
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 트랜잭션 범위의 영속성 컨텍스트
* 스프링 컨테이너는 기본으로 트랜잭션 범위의 영속성 컨텍스트 전략 사용
  * 트랜잭션 범위와 영속성 컨텍스트의 생존 범위가 같음

### 트랜잭션 커밋 & 롤백
* 커밋 전, 영속성 컨텍스트를 플러시하여 데이터베이스에 반영
* 롤백시, 영속성 컨텍스트를 플러시하지 않음

### 트랜잭션이 같은 경우 & 다른 경우 
* 트랜잭션이 같은 경우, 항상 같은 영속성 컨텍스트를 사용
* 트랜잭션이 다른 경우, 접근하는 영속성 컨텍스트가 다름
  * 스프링 컨테이너는 스레드마다 각각 다른 트랜잭션 할당

## 준영속 상태와 지연 로딩
* 프레젠테이션 레이어에서 준영속 상태가 되어 지연 로딩이 불가능
* 3가지 방안
  1. 글로벌 페치 전략 수정
  2. JPQL 페치 조인
  3. 강제로 초기화

### 글로벌 페치 전략 수정
* `FetchType.LAZY` -> `FetchType.EAGER`

#### 단점
1. 사용하지 않는 엔티티 로딩
2. N+1 문제가 발생
    * JPQL 을 분석해서 SQL 생성시, 글로벌 페치 전략을 참고하지 않고 JPQL 자체만 사용
    * 조회한 엔티티가 참조하는 `FetchType.EAGER` 엔티티를 바로 조회

### JPQL 페치 조인
* JPQL 을 이용한 페치 조인

#### 단점
* 뷰와 리포지토리 간에 논리적인 의존 관계 발생

### 강제로 초기화
* 프레젠테이션 계층에서 필요한 필드를 조회하여 엔티티를 강제로 초기화
* `org.hibernate.Hibernate.initialize(Object Any)` 로도 초기화 가능
* 이때, 서비스 계층에서 처리하기보다, 프레젠테이션 계층과 서비스 계층 사이에 FACADE 계층을 두어 초기화 하는 방안 존재
  * 프레젠테이션 -> FACADE -> 서비스


## OSIV (Open Session In View)
* 모든 문제는 엔티티가 프레젠테이션 레이어에서 준영속 상태이기 때문에 발생
* 프레젠테이션 레이어까지 영속성 컨텍스트 유지 (조회 가능, 수정 불가능)
  * 트랜잭션 종료 이후 엔티티를 수정하더라도 DB 에 반영하지 않음
* 단, 트랜잭션은 서비스 레이어까지만 존재 (조회 및 수정 가능)
* **같은 영속성 컨텍스트**를 **여러 트랜잭션**이 공유
  * 엔티티 수정후, 다른 트랜잭션 진행시, 수정된 엔티티가 DB 에 반영됨

### 설정
* 필터 혹은 인터셉터를 등록
* 스프링부트의 경우 `HibernateJpaAutoConfiguration` 에 의해 `OpenEntityManagerInViewInterceptor` 가 등록됨

```
org.springframework.orm.hibernate5.support.OpenSessionInViewFilter
org.springframework.orm.hibernate5.support.OpenSessionInViewInterceptor
org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor
```

