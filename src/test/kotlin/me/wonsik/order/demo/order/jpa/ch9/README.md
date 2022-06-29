# 값 타입
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 특징 및 종류

### 특징
* 식별자가 없다
* 생명 주기를 엔티티에 의존
* 공유하지 않는 것이 안전
  * 공유하지 않는 것이 안전
  * 오직 하나의 주인만이 관리해야한다
  * 불변 객체로 만드는것이 안전

### 종류

* 값 타입은 3가지 종류가 존재
* 기본값 타입
  * 자바 기본 타입 (int, double)
  * 래퍼 클래스 (Integer)
  * String
* 임베디드 타입 (복합 값 타입)
* 컬렉션 타입


## 임베디드 타입 (복합 값 타입)

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    val id: Long? = null,
    @Embedded
    val period: Period,
    @Embedded
    @AttributeOverrides(
      AttributeOverride(name = "startDate", column = Column(name = "company_start_date", nullable = true)),
      AttributeOverride(name = "endDate", column = Column(name = "company_end_date", nullable = true))
    )
    val companyPeriod: Period?,
)

@Embeddable
class Period(
    @Column(nullable = false)
    val startDate: LocalDate,
    @Column(nullable = false)
    val endDate: LocalDate,
)
```

* `@Embeddable`: 값 타입을 정의하는 곳에 표시
* `@Embedded`: 값 타입을 사용하는 곳에 표시 
* `@AttributeOverrides`, `@AttributeOverride`: 임베디드 타입에 정의한 매핑정보를 재정의
* 임베디드 타입이 null 이면 매핑한 컬럼 값은 모두 null 이 됨

## 값 타입과 불변 객체
* 값 타입 공유 참조
  * Side Effect 발생 가능 -> 값을 복사해서 사용
* 값 타입 복사
  * 복사하지 않고 원본의 참조를 직접 넘기는 것을 막을 방법이 없음
  * 객체의 값을 수정하지 못하게 막으면 간단하게 해결 가능
* 값 타입은 가능한 **불변 객체** 로 설계

## 값 타입의 비교
* `equals` 를 사용해서 동등성 비교
* `equals` 재정의시, 보통 모든 필드 값을 비교하도록 구현
  * `hashCode` 도 동일하게 모든 필드 값을 사용하도록 구현

## 값 타입 컬렉션

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOODS", joinColumns = [JoinColumn(name = "MEMBER_ID")])
    @Column(name = "FOOD_NAME")
    val favoriteFoods: Set<String> = hashSetOf(),

    @ElementCollection
    @CollectionTable(name = "ADDRESS_HISTORY", joinColumns = [JoinColumn(name = "MEMBER_ID")])
    @Column(name = "FOOD_NAME")
    val addressHistory: List<Address> = arrayListOf(),
)

@Embeddable
data class Address (
    @Column val state: String,
    @Column val city: String,
    @Column val street: String,
    @Column val subStreet: String = "",
    @Column val zipCode: Int
)
```

* PK 가 전체 컬럼으로 구성 (not null)
* `FetchType.LAZY` 가 디폴트
* 영속성 전이, 고아 객체 제거 기능을 필수로 가짐
* 변경 사항이 생기면 모든 데이터를 삭제하고 현재 가지고 있는 모든 값을 다시 저장
* 가능하면 엔티티를 사용할것을 권장