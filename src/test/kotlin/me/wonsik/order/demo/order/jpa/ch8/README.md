# 프록시와 연관관계 관리
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 프록시
* 지연 로딩시, 실제 엔티티 객체 대신, DB 조회를 지연하는 프록시 객체 사용
* 실제 사용될때, 데이터베이스 조회해서 엔티티 객체 생성
* `EntityManager#getReference` 를 통해 프록시 객체 조회

### 특징
* 프록시 객체는 처음 사용할때만 초기화 됨 (영속성 컨텍스트의 도움을 통해)
* 프록시 객체를 초기화 한다고 엔티티 객체로 바꿔치기 되는것은 아님
* 프록시 객체는 원본 엔티티를 상속한 객체
* 영속성 컨텍스트에 이미 존재시, `EntityManager#getReference` 호출해도 엔티티 반환
* 준영속 상태의 프록시 초기화시, 예외 발생
  * 하이버네이트는 `org.hibernate.LazyInitializationException` 를 던짐

### 식별자
* 프록시 객체는 식별자 값을 가지고 있음
* 연관 관계 설정시, 식별자 값만 사용하므로 프록시를 사용하면 조회하지 않고 설정 가능

## 즉시 로딩과 지연 로딩
* 즉시 로딩
  * 엔티티를 조회할때, 연관된 엔티티도 함께 조회
  * `FetchType.EAGER`
  * 디폴트: `@OneToOne`, `@ManyToOne`
* 지연 로딩
  * 연관된 엔티티를 실제 사용할때 조회
  * `FetchType.LAZY`
  * 디폴트: `@OneToMany`, `@ManyToMany`
  * 조회 대상이 이미 영속성 컨텍스트에 있으면 프록시가 아닌 실제 객체 사용
  * 컬렉션은 컬렉션 래퍼가 지연 로딩을 처리

### NULL 제약조건
* `@JoinColumn(nullable = true)`: NULL 허용, 외부 조인 사용
* `@JoinColumn(nullable = false)`: NULL 허용하지 않음, 내부 조인 사용

### FetchType.EAGER 인 경우, 조인 전략
* `@ManyToOne`, `@OneToOne`
  * (optional = false) : 내부 조인
  * (optional = true) : 외부 조인
* `@ManyToMany`, `@OneToMany` 
  * 항상 외부 조인


## 영속성 전이
* 특정 엔티티를 영속 상태로 만들때, 연관된 엔티티도 함께 영속 상태로 만들고 싶다면 영속성 전이 사용
* 영속성 전이는 연관 관계를 매핑하는것과는 **아무 관련이 없다**.


## 고아 객체
* 부모 엔티티와 연관 관계가 끊어진 자식 엔티티를 자동으로 삭제
* `@OneToOne`, `@OneToMany` 에만 적용 가능

## 영속성 전이 + 고아 객체, 생명 주기
* 부모 엔티티를 통해서 자식의 생명 주기 관리 가능
* DDD 의 Aggregate Root 개념을 구현할때 편리
