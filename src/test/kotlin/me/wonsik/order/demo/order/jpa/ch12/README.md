# 스프링 데이터 JPA
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## JpaRepository 기능

### JpaRepository 계층 구조

```java
public interface Repository<T, ID> {}

public interface CrudRepository<T, ID> extends Repository<T, ID> {
    <S extends T> S save(S entity);
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> ids);
    long count();
    void deleteById(ID id);
    void delete(T entity);
    void deleteAllById(Iterable<? extends ID> ids);
    void deleteAll(Iterable<? extends T> entities);
    void deleteAll();
}

public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {
    Iterable<T> findAll(Sort sort);
    Page<T> findAll(Pageable pageable);
}

public interface JpaRepository<T, ID> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {
    @Override List<T> findAll();
    @Override List<T> findAll(Sort sort);
    @Override List<T> findAllById(Iterable<ID> ids);
    @Override <S extends T> List<S> saveAll(Iterable<S> entities);
    void flush();
    <S extends T> S saveAndFlush(S entity);
    <S extends T> List<S> saveAllAndFlush(Iterable<S> entities);
    void deleteAllInBatch(Iterable<T> entities);
    void deleteAllByIdInBatch(Iterable<ID> ids);
    void deleteAllInBatch();
    T getById(ID id);
    @Override
    <S extends T> List<S> findAll(Example<S> example);
    @Override
    <S extends T> List<S> findAll(Example<S> example, Sort sort);
}
```

### 쿼리 메소드 기능

#### 메소드 이름으로 쿼리 생성
* 인터페이스 메소드 명으로 쿼리 생성
* https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-keywords

#### 메소드 이름으로 JPA NamedQuery 호출
* 일반적으로 해당 엔티티 클래스 선언에 `@NamedQuery` 쿼리 정의
  * 쿼리명을 `<도메인 클래스>.<메소드 이름>` 로 정의
* 스프링 Data JPA 에서는 `<도메인 클래스>.<메소드 이름>` 으로 Named 쿼리를 찾아 실행

#### Query 어노테이션을 사용해서 리포지토리 인터페이스에 쿼리 직접 정의
* 인터페이스 메서드에 `@Query` 어노테이션 정의


### 스프링 Data JPA 가 사용하는 구현체
* 스프링 Data JPA 가 제공하는 공통 인터페이스는 `org.springframework.data.jpa.repository.support.SimpleJpaRepository` 클래스가 구현함

#### `@Repository`
* JPA 예외를 스프링이 추상화한 예외로 변환

#### `@Transactional`
* JPA 의 모든 변경은 트랜잭션 안에서 이루어져야 함
* 서비스 계층에서 트랜잭션을 시작하지 않으면 리포지토리에서 시작
* 서비스 계층에서 트랜잭션을 시작시 해당 트랜잭션 사용

#### `save(S entity)`
* 새로운 엔티티면 저장, 이미 있는 엔티티면 병합
* 식별자가 객체인 경우 null, 식별자가 기본 타입인 경우 0 이면 새로운 엔티티로 판단

### 힌트와 락
#### 힌트
* JPA 구현체에게 힌트 전달 필요시 사용
* `@QueryHint`, `@QueryHints` 사용

#### 락
* `@Lock` 사용
