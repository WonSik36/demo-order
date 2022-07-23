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


## 성능 최적화

