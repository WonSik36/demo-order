# 고급 매핑
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.


## 상속 관계 매핑
### 조인 전략
* 슈퍼 타입 테이블과 서브 타입 테이블 모두 테이블로 만들고 조회할때는 조인 사용

```kotlin
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    val id: Long? = null,
    val name: String,
    val price: Int
) {}

@Entity
@DiscriminatorValue("A")
class Album(
    id: Long? = null,
    name: String,
    price: Int,
    val artist: String
): Item(id, name, price) {}
```

#### 장점
* 테이블이 정규화됨
* 외래 키 참조 무결성 제약조건 활용 가능
* 저장 공간을 효율적으로 사용

#### 단점
* 조회할 때 조인이 많이 사용되므로 성능이 저하될 수 있음
* 조회 쿼리가 복잡
* 데이터를 등록할때, INSERT 쿼리를 두 번 실행

### 단일 테이블 전략
* 하나의 테이블만 사용

```kotlin
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item(
    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    val id: Long? = null,
    val name: String,
    val price: Int
) {}

@Entity
@DiscriminatorValue("A")
class Album(
    id: Long? = null,
    name: String,
    price: Int,
    val artist: String
): Item(id, name, price) {}
```

#### 장점
* 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
* 조회 쿼리가 단순

#### 단점
* 자식 엔티티가 매핑한 컬럼은 모두 null 을 허용해야함
* 단일 테이블에 모든 것을 저장하므로 테이블이 커질수 있음

### 구현 클래스마다 테이블 전략
* 서브 타입마다 하나의 테이블을 만듦
  * 슈퍼 타입은 만들지 않음
* 일반적으로 사용하지 않는 전략

```kotlin
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class Item(
    @Id
    @GeneratedValue
    @Column(name = "ITEM_ID")
    val id: Long? = null,
    val name: String,
    val price: Int
) {}

@Entity
class Album(
    id: Long? = null,
    name: String,
    price: Int,
    val artist: String
): Item(id, name, price) {}
```

#### 장점
* 서브 타입을 구분해서 처리할때 효과적
* not null 제약 조건 사용 가능

#### 단점
* 여러 자식 테이블을 함께 조회할 때 성능이 느림
* 자식 테이블을 통합해서 쿼리하기 어려움



## @MappedSuperclass
* 단순히 매핑 정보를 상속할 목적으로만 사용
* `@Entity` 는 실제 테이블과 매핑되지만 `@MappedSuperclass` 는 실제 테이블과 매핑되지 않음
* 매핑 정보를 **재정의** 하려면 `@AttributeOverrides` 와 `@AttributeOverride` 사용

```kotlin
@MappedSuperclass
abstract class BaseEntity (
    @Id
    @GeneratedValue
    val id: Long? = null,
    val name: String,
) {}

@Entity
@AttributeOverrides(
    AttributeOverride(name = "id", column = Column(name = "MEMBER_ID")),
    AttributeOverride(name = "name", column = Column(name = "MEMBER_NAME"))
)
class Member (
    id: Long?,
    name: String,
    val email: String
): BaseEntity(id, name)
```



## 복합 키와 식별 관계 매핑
* 식별 관계
  * 부모 테이블의 기본 키를 받아서 **자식 테이블의 기본키** + **외래키**로 사용하는 관계
* 비식별 관계
  * 부모 테이블의 기본 키를 받아서 자식 테이블의 외래 키로만 사용하는 관계 
  * 필수적 비식별 관계
    * 외래키에 NULL 을 허용하지 않는다. 연관 관계를 필수적으로 맺어야 한다.
  * 선택적 비식별 관계
    * 외래키에 NULL 을 허용한다. 연관 관계를 맺을지 말지 선택할 수 있다.

### `@IdClass`

```kotlin
@Entity
@IdClass(ParentId::class)
class Parent(
    @Id
    @Column(name = "PARENT_ID1")
    val id1: String,
    @Id
    @Column(name = "PARENT_ID2")
    val id2: String,
)

class ParentId(
    val id1: String,    // Parent.id1 매핑
    val id2: String     // Parent.id2 매핑
): Serializable {
  // equals, hashCode
}

@Entity
class Child(
  @Id
  val id: String,
  @ManyToOne
  @JoinColumns(
    JoinColumn(name = "PARENT_ID1_CHILD", referencedColumnName = "PARENT_ID1"),
    JoinColumn(name = "PARENT_ID2_CHILD", referencedColumnName = "PARENT_ID2")
  )
  val parent: Parent
)
```

* 식별자 클래스의 속성명과 엔티티에서 사용하는 식별자의 속성명이 같아야 한다.
* `Serializable` 인터페이스를 구현해야 한다.
* `equals`, `hashCode` 를 구현해야 한다.
* 기본 생성자(noArgs) 가 있어야 한다.
* 식별자 클래스는 public 이어야 한다.

### `@EmbeddedId`

```kotlin
@Entity
class Parent(
    @EmbeddedId
    val id: ParentId
)

@Embeddable
class ParentId(
    @Column
    val id1: String,
    @Column
    val id2: String
): Serializable {
    // equals, hashCode
}

@Entity
class Child(
    @EmbeddedId
    val id: ChildId,
    @MapsId("parentId")   // ChildId.parentId 매핑
    @ManyToOne
    @JoinColumns(
      JoinColumn(name = "PARENT_ID1", referencedColumnName = "id1"),
      JoinColumn(name = "PARENT_ID2", referencedColumnName = "id2")
    )
    val parent: Parent
)

@Embeddable
class ChildId(
    @Column
    val parentId: ParentId,
    @Column
    val id: String
): Serializable {
  // equals, hashCode
}
```

* `@Embeddable` 어노테이션을 붙어주어야 한다.
* `Serializable` 인터페이스를 구현해야 한다.
* `equals`, `hashCode` 를 구현해야 한다.
* 기본 생성자(noArgs) 가 있어야 한다.
* 식별자 클래스는 public 이어야 한다.

### `@IdClass` vs `@EmbeddedId`
* `@EmbeddedId` 가 `@IdClass` 와 비교해서 더 객체지향적이고 중복도 없으나, 특정 상황에 JPQL 이 더 길어질 수 있다.
* 비식별 관계로 구현시, 복합 키를 사용한 코드와 비교하면 매핑도 쉽고 코드도 단순해짐

### 일대일 식별 관계
* 자식 테이블의 기본 키 값으로 부모 테이블의 기본 키 값만 사용

```kotlin
@Entity
class Board(
    @Id
    @GeneratedValue
    @Column(name = "BOARD_ID")
    val id: Long? = null,
    @OneToOne
    val boardDetail: BoardDetail
)

@Entity
class BoardDetail(
    @Id
    @Column(name = "BOARD_ID")
    val boardId: Long? = null,
    @MapsId
    @OneToOne
    @JoinColumn(name = "BOARD_ID")
    val board: Board
)
```



## 조인 테이블
* 별도의 테이블을 사용해서 연관관계 관리
* 기본은 조인 컬럼, 필요하다고 판단되면 조인 테이블 사용

### 일대일

```kotlin
@Entity
class Parent(
    @Id
    @GeneratedValue
    @Column(name = "PARENT_ID")
    val id: Long? = null,
    @OneToOne
    @JoinTable(
        name = "PARENT_CHILD",
        joinColumns = [JoinColumn(name = "PARENT_ID")],
        inverseJoinColumns = [JoinColumn(name = "CHILD_ID")],
    )
    val child: Child
)

@Entity
class Child(
    @Id
    @GeneratedValue
    @Column(name = "CHILD_ID")
    val id: Long? = null,
)
```
* `PARENT_ID` (PK,FK)
* `CHILD_ID` (UNI,FK)


### 일대다

```kotlin
@Entity
class Parent(
    @Id
    @GeneratedValue
    @Column(name = "PARENT_ID")
    val id: Long? = null,
    @OneToMany
    @JoinTable(
        name = "PARENT_CHILD",
        joinColumns = [JoinColumn(name = "PARENT_ID")],
        inverseJoinColumns = [JoinColumn(name = "CHILD_ID")],
    )
    val children: MutableList<Child> = arrayListOf()
)

@Entity
class Child(
    @Id
    @GeneratedValue
    @Column(name = "CHILD_ID")
    val id: Long? = null,
)
```
* `PARENT_ID` (FK)
* `CHILD_ID` (PK,FK)


### 다대일

```kotlin
@Entity
class Parent(
    @Id
    @GeneratedValue
    @Column(name = "PARENT_ID")
    val id: Long? = null,
    @ManyToOne(optional = false)
    @JoinTable(
        name = "PARENT_CHILD",
        joinColumns = [JoinColumn(name = "PARENT_ID")],
        inverseJoinColumns = [JoinColumn(name = "CHILD_ID")],
    )
    val child: Child
)

@Entity
class Child(
    @Id
    @GeneratedValue
    @Column(name = "CHILD_ID")
    val id: Long? = null,
)
```

* `PARENT_ID` (PK,FK)
* `CHILD_ID` (FK)


### 다대다

```kotlin
@Entity
class Parent(
    @Id
    @GeneratedValue
    @Column(name = "PARENT_ID")
    val id: Long? = null,
    @ManyToMany
    @JoinTable(
        name = "PARENT_CHILD",
        joinColumns = [JoinColumn(name = "PARENT_ID")],
        inverseJoinColumns = [JoinColumn(name = "CHILD_ID")],
    )
    val children: MutableList<Child> = arrayListOf()
)

@Entity
class Child(
    @Id
    @GeneratedValue
    @Column(name = "CHILD_ID")
    val id: Long? = null,
)
```

* `PARENT_ID` (PK,FK)
* `CHILD_ID` (PK,FK)

## 엔티티 하나에 여러 테이블 매핑

* 한 엔티티에 여러 테이블 매핑

```kotlin
@Entity
@Table(name = "BOARD")
@SecondaryTable(
    name = "BOARD_DETAIL",
    pkJoinColumns = [PrimaryKeyJoinColumn(name = "BOARD_DETAIL_ID")]
)
class Board(
    @Id
    @GeneratedValue
    @Column(name = "BOARD_ID")
    val id: Long? = null,
    val title: String,
    @Column(table = "BOARD_DETAIL")
    val content: String
) {}
```