# 컬렉션과 부가 기능
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 컬렉션
* 하이버네이트는 엔티티를 영속 상태로 만들때, 컬렉션 필드를 래핑함
  * 원본 컬렉션이 null 이 아니여야함
* `Collection, List` 는 엔티티를 추가할때, 중복된 엔티티가 있는지 비교하지 않음
  * 따라서 엔티티를 추가해도 지연 로딩된 컬렉션을 초기화하지 않음
* `Set` 은 엔티티를 추가할때, 중복된 엔티티가 있는지 비교해야함
  * 따라서 엔티티를 추가할때, 지연로딩된 컬렉션을 초기화함

| 컬렉션 인터페이스           | 내장 컬렉션          | 중복 허용 | 순서 보관 | 권장하는 원본 컬렉션 |
|---------------------|-----------------|-------|-------|-------------|
| Collection, List    | PersistentBag   | O     | X     | ArrayList   |
| Set                 | PersistentSet   | X     | X     | HashSet     |
| List + @OrderColumn | PersistentList  | O     | O     | ArrayList   |

* `@OrderColumn` 의 경우, 단점이 존재하여 순서가 필요한 경우 직접 관리하는것을 권장

## @Converter
* `@Converter` 어노테이션 적용 및 `AttributeConverter` 인터페이스를 구현
* 필드 레벨, 클래스 레벨, 전역 레벨 모두 적용 가능
  * 필드 레벨, 클래스 레벨: `@Convert` 어노테이션 사용
  * 전역 레벨: `@Converter(autoApply = true)` 사용


## 리스너
* 엔티티 생명주기에 따른 이벤트 처리

### 이벤트 종류
* PostLoad
  * 엔티티가 영속성 컨텍스트로 로딩된 이후
* PrePersist
  * 새로운 엔티티를 영속성 컨텍스트에 추가하기 전
* PostPersist
  * 새로운 엔티티가 데이터 베이스에 저장된 이후
* PreUpdate
  * flush 나 commit 을 호출하여 엔티티가 수정되기 전
* PostUpdate
  * flush 나 commit 을 호출해서 엔티티가 수정된 이후
* PreRemove
  * 엔티티를 삭제하기 전
* PostRemove
  * flush 나 commit 을 호출해서 엔티티를 삭제한 이후


### 적용 위치
* 엔티티에 직접 적용
  * 이벤트 종류에 따른 메소드 등록
* 별도의 리스너 등록
  * `@EntityListener`
* 기본 리스너 사용
  * `org.hibernate.event.service.spi.EventListenerRegistry` 에 리스너 등록

### 호출 순서
1. 기본 리스너
2. 부모 클래스 리스너
3. 리스너
4. 엔티티

## 엔티티 그래프
* 엔티티를 조회하는 시점에 함께 조회할 엔티티를 선택할수 있음
* 힌트와 엔티티 그래프 객체를 전달

### 힌트
* `javax.persistence.fetchgraph`: 엔티티 그래프에 선택한 속성만 함께 조회
* `javax.persistence.loadgraph`: 엔티티 그래프에 선택한 속성 + 글로벌 페치모드가 `FetchType.EAGER` 로 설정된 연관 관계 포함

### Named 엔티티 그래프
* 세개 어노테이션 활용
  * `@NamedEntityGraph`: 루트 어노테이션
  * `@NamedAttributeNode`: 엔티티 그래프 멤버
  * `@NamedSubgraph`: 서브 그래프

### 동적 엔티티 그래프
* `EntityManager#createEntityGraph` 을 활용하여 `EntityGraph<E>` 생성
* 서브그래프(`Subgraph<X>`) 는 `EntityGraph<E>#addSubgraph` 로 생성 가능

### QueryDSL
* `AbstractJPAQuery#setHint` 를 사용하여 힌트와 `EntityGraph<E>` 전달