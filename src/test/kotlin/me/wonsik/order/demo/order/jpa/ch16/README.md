# 트랜잭션과 락, 2차 캐시
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.


## 트랜잭션과 락

### ACID

* 원자성 (Atomicity)
  * 트랜잭션 내에서 실행한 작업들은 마치 하나의 작업인것처럼 모두 성공하거나 모두 실패해야한다.
* 일관성 (Consistency)
  * 모든 트랜잭션은 일관성 있는 데이터베이스 상태를 유지해야 한다. 
  * 예를 들어 데이터베이스에서 정한 무결성 제약 조건을 항상 만족해야한다.
* 격리성 (Isolation)
  * 동시에 실행되는 트랜잭션들은 서로에게 영향을 미치지 않도록 격리한다.
  * 예를 들어 동시에 같은 데이터를 수정하지 못하게 해야한다.
  * 격리성은 동시성과 관련된 성능 이슈로 인해 격리 수준을 선택할수 있다.
* 지속성 (Durability)
  * 트랜잭션을 성공적으로 끝내면 그 결과가 항상 기록되어야 한다.
  * 중간에 시스템에 문제가 발생해도 데이터베이스 로그 등을 사용해서 성공한 트랜잭션 내용을 복구해야한다.

### 트랜잭션 격리 수준
* 순서대로 격리 수준이 올라감
* 격리 수준이 낮을수록 동시성은 증가하지만 격리 수준에 따른 다양한 문제 발생 가능

| 격리 수준            | DIRTY READ | NON-REPEATABLE READ | UPDATE 부정합 | PHANTOM READ |
|------------------|------------|---------------------|------------|--------------|
| READ UNCOMMITTED | O          | O                   | O          | O            |
| READ COMMITED    |            | O                   | O          | O            |
| REPEATABLE READ  |            |                     | O          | O            |
| SERIALIZABLE     |            |                     |            |              |

* DIRTY READ
  * 처음 데이터 조회후
  * 다른 트랜잭션에서 커밋하지 않은 상태로 데이터 수정 
  * 다시 데이터 조회시 결과가 다름
* NON-REPEATABLE READ
  * 처음 데이터 조회후
  * 다른 트랜잭션에서 데이터를 수정후 커밋
  * 다시 데이터 조회시 결과가 다름
* UPDATE 부정합
  * 처음 데이터 조회후
  * 다른 트랜잭션에서 해당 데이터 조건으로 조회후 수정
  * 현재 트랜잭션에서 해당 조건을 기준으로 데이터 수정했으나 업데이트가 되지 않음
* PHANTOM READ
  * 처음 데이터 조회시 존재하지 않음 
  * 다른 트랜잭션에서 데이터를 추가
  * 다시 조회시, 데이터가 새로 발견됨

### 낙관적 락과 비관적 락
* JPA 는 데이터베이스 트랜잭션 격리 수준을 READ COMMITTED 정도로 가정
* 더 높은 격리 수준이 필요할시, 낙관적 락과 비관적 락 둘 중 하나 사용

#### 낙관적 락
* JPA 가 제공하는 버전 관리 기능 사용
* 버전 컬럼을 두어 업데이트시, 조건으로 사용
  * 버전은 엔티티의 값이 변경되면 증가 
* 업데이트가 일어나지 않는다면 충돌이 발생한것이므로 예외 발생

#### 비관적 락
* 데이터베이스가 제공하는 락 기능을 사용


### JPA 락 사용

#### 락 적용 가능 위치
* `EntityManager#lock, EntityManager#find, EntityManager#refresh`
* `Query#setLockMode`
* `@NamedQuery`

#### JPA 락 예외
* 낙관적 락
  * JPA 예외: `OptimisticLockException`
  * 하이버네이트 예외: `StaleStateException`
  * 스프링 예외 추상화: `ObjectOptimisticLockingFailureException`
* 비관적 락
  * JPA 예외: `PessimisticLockException`
  * 하이버네이트 예외: `PessimisticLockException`
  * 스프링 예외 추상화: `PessimisticLockingFailureException`

#### `LockModeType`
* `OPTIMISTIC`
  * 낙관적 락 사용
  * 엔티티를 조회만 해도 커밋할때 버전을 체크
* `OPTIMISTIC_FORCE_INCREMENT`
  * 낙관적 락 + 버전정보 강제 증가
* `PESSIMISTIC_READ`
  * 비관적 락, 읽기 락 사용
* `PESSIMISTIC_WRITE`
  * 비관적 락, 쓰기 락 사용
* `PESSIMISTIC_FORCE_INCREMENT`
  * 비관적 락 + 버전정보 강제 증가
* `NONE`
  * 엔티티에 `@Version` 이 적용된 필드가 있으면 낙관적 락이 적용됨

