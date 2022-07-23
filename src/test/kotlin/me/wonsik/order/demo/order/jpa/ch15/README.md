# 고급 주제와 성능 최적화
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.


## 예외 처리
### JPA 표준 예외
* JPA 표준 예외들은 `javax.persistence.PersistenceException` 를 상속한다.
* `javax.persistence.PersistenceException` 는 `RuntimeException` 을 상속

#### `marked for rollback` 을 기준으로 JPA 표준 예외 분류
* 아래 4개 예외를 제외한 `PersistenceException` 은 `EntityTransaction#getRollbackOnly` 값을 `true` 로 만든다.
  * NoResultException
  * NonUniqueResultException
  * LockTimeoutException
  * QueryTimeoutException
* `EntityTransaction#getRollbackOnly` 값이 
  * `true` 인 경우, 강제로 커밋이 불가능하다.
  * `false` 인 경우, 강제로 커밋이 가능하다.
* 테스트 결과, 상황별로 `rollbackOnly` 마킹을 안하는 경우가 존재
  * 따라서 왠만하면 예외 발생시, 롤백 처리하는게 바람직해 보임

### 스프링 프레임워크의 JPA 예외 변환
* `PersistenceExceptionTranslator` 는 `PersistenceException` 를 `DataAccessException` 로 변환
* `PersistenceExceptionTranslationPostProcessor` 빈으로 등록시, `@Repository` 어노테이션이 적용된 곳에 AOP 로 `PersistenceExceptionTranslator` 적용

### 주의 사항
* 트랜잭션을 롤백하는 것은 데이터베이스의 반영사항만 롤백, 수정한 자바 객체를 원상태로 복구하지는 않음
* 새로운 영속성 컨텍스트를 생성해서 사용하거나, 초기화(`EntityManager#clear`) 한 다음에 사용해야함
  * `SimpleJpaRepository` 에 `@Repository` 가 적용되어 있음

#### 스프링: 영속성 컨텍스트 범위 = 트랜잭션 범위
* 트랜잭션 종료 시점에 영속성 컨텍스트도 종료하므로 문제 없음

#### 스프링: 영속성 컨텍스트 범위 > 트랜잭션 범위
* 트랜잭션 롤백시, 영속성 컨텍스트를 초기화함 (`EntityManager#clear`)
* `JpaTransactionManager#doRollback` 참조



## 엔티티 비교
### 동일한 영속성 컨텍스트인 경우
* 동일성: == 비교가 같다 (같은 주소를 지니고 있다)
* 동등성: equals 비교가 같다
* 데이터베이스 동등성: 식별자가 같다

### 다른 영속성 컨텍스트인 경우
* 동일성: == 비교가 다르다 (다른 주소를 지니고 있다)
* 동등성: equals 비교가 같다
* 데이터베이스 동등성: 식별자가 같다



## 프록시 심화 주제
### 영속성 컨텍스트와 프록시
* 영속성 컨텍스트는 자신이 관리하는 엔티티의 동일성을 보장
* 따라서 프록시 조회후 엔티티 조회시, 동일 객체 반환
* 반대의 경우도 마찬가지로 동일 객체 반환

### 프록시 타입 비교
* 프록시는 원본 엔티티를 상속 받아서 만들어짐
* `instanceof`(자바) 혹은 `is`(코틀린) 사용

### 프록시 동등성 비교
* `Proxy.equals(Entity)` 시, 내부적으로 `Target.equals(Entity)` 호출
* `Entity.equals(Proxy)` 시, 주의사항
  1. 타입 비교시, `instanceof`(자바) 혹은 `is`(코틀린) 사용
  2. 멤버 변수에 접근자 메소드를 사용하여 접근

### 상속 관계와 프록시
* 상속 관계 매핑한 상태에서 **프록시**를 **부모 타입**으로 조회하면, 자식 타입으로 타입 캐스팅이 불가능

#### 해결방법
1. 자식 타입으로 조회
2. 프록시를 벗겨서 원본 엔티티 조회
  * 프록시와 원본 엔티티 사이의 동일성 비교가 실패함
3. 다형성 활용 (인터페이스 정의)
4. 비지터 패턴 활용


## 성능 최적화

### N+1 문제
* 즉시 로딩
  * JPQL 을 실행할때, N+1 문제가 발생할수 있음
* 지연 로딩
  * 컬렉션으로 연산중에 지연 로딩을 한다면, N+1 문제가 발생할수 있음

#### 해결 방안
1. 페치 조인 사용
2. 하이버네이트 `@BatchSize`
   * 연관된 엔티티 조회시, 지정한 size 만큼 IN 절을 활용하여 조회
3. 하이버네이트 `@Fetch(FetchMode.SUBSELECT)`
   * 연관된 데이터를 조회할때, 서브 쿼리에 조회 조건을 넣어줌


### 읽기 전용 쿼리의 성능 최적화
* 엔티티가 영속성 컨텍스트에 관리되면 변경 감지를 위해 스냅샷 인스턴스를 보관해야하므로 더 많은 메모리를 사용해야함
* 읽기 전용 쿼리 사용시, 메모리 사용을 줄일수 있음

#### 스칼라 타입으로 조회
* 스칼라 타입은 영속성 컨텍스트가 관리하지 않는다

#### 읽기 전용 쿼리 힌트 사용
* 하이버네이트의 `org.hibernate.readOnly` 힌트 사용시, 엔티티를 읽기 전용으로 조회
* 영속성 컨텍스트는 스냅샷을 보관하지 않음

#### 읽기 전용 트랜잭션 사용
* `@Transactional(readOnly = true)` 사용시, 트랜잭션을 읽기 전용 모드로 설정
* 스프링에서 하이버네이트의 플러시 모드를 `MANUAL` 로 설정
  * 해당 모드에서는 강제로 플러시를 호출하지 않으면 절대 플러시가 발생하지 않음

#### 트랜잭션 밖에서 읽기 
* `@Transactional(propagation = Propagation.NOT_SUPPORTED)` 를 사용하여 트랜잭션 없이 엔티티 조회
* 트랜잭션이 없어 커밋되지 않으므로, 플러시가 호출되지 않음


### 배치 처리
#### 등록 배치 처리
* 주기적으로 `flush` 및 `clear` 호출

#### 수정 배치 처리
* JPA 페이징 배치 처리
* 하이버네이트 scroll 사용
* 하이버네이트 무상태 세션 사용
  * 영속성 컨텍스트가 존재하지 않음


### SQL 쿼리 힌트 사용
* JPA 에서는 SQL 힌트 기능을 제공하지 않음
* 하이버네이트 구현체를 통해 SQL 힌트 기능 사용 가능

