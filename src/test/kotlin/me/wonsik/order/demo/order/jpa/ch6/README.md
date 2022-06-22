# 다양한 연관관계 매핑
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 다대일

### 다대일 단방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    val team: Team
) {}

@Entity
class Team(
    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    val id: Long? = null,
) {}
```

### 다대일 양방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    val team: Team
) {}

@Entity
class Team(
    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    val id: Long? = null,
    @OneToMany(mappedBy = "team")
    val members: MutableList<Member> = arrayListOf()
) {}
```

## 일대다

### 일대다 단방향

```kotlin
@Entity
class Team(
    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    val id: Long? = null,
    @OneToMany
    @JoinColumn(name = "TEAM_ID")   // MEMBER 테이블의 TEAM_ID (FK)
    val members: MutableList<Member> = arrayListOf()
) {}

@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
) {}
```

* 연관 관계 처리를 위한 UPDATE 쿼리를 별도로 실행
* 일대다 단방향 보다 다대일 양방향 사용 


### 일대다 양방향

```kotlin

@Entity
class Team(
    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    val id: Long? = null,
    @OneToMany
    @JoinColumn(name = "TEAM_ID")   // MEMBER 테이블의 TEAM_ID (FK)
    val members: MutableList<Member> = arrayListOf()
) {}

@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    
    // Team.members 에서 TEAM_ID 를 관리하므로 Member.team 에서는 관리하지 않도록 수정
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
    val team: Team
) {}
```

* 일대다 양방향 보다 다대일 양방향 사용 


## 일대일

### 주 테이블에 외래키: 일대일 단방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    var locker: Locker?
) {}

@Entity
class Locker(
    @Id
    @GeneratedValue
    @Column(name = "LOCKER_ID")
    val id: Long? = null
) {}
```

### 주 테이블에 외래키: 일대일 양방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    var locker: Locker?
) {}

@Entity
class Locker(
    @Id
    @GeneratedValue
    @Column(name = "LOCKER_ID")
    val id: Long? = null,
    @OneToOne(mappedBy = "locker")
    val member: Member
) {}
```

### 대상 테이블에 외래키: 일대일 단방향
* 일대다 단방향 관계에서는 대상 테이블에 외래키가 있는 경우를 허용
* 일대일 단방향에서는 허용하지 않음

### 대상 테이블에 외래키: 일대일 양방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @OneToOne(mappedBy = "member")
    var locker: Locker?
) {}

@Entity
class Locker(
    @Id
    @GeneratedValue
    @Column(name = "LOCKER_ID")
    val id: Long? = null,
    @OneToOne
    @JoinColumn(name = "MEMBER_ID")
    val member: Member
) {}
```
* 추후 일대다로 수정 가능


## 다대다

### 단방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @ManyToMany
    @JoinTable(
        name="MEMBER_PRODUCT",
        joinColumns = [JoinColumn(name = "MEMBER_ID")],
        inverseJoinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    val products: MutableList<Product> = arrayListOf()
) {}

@Entity
class Product(
    @Id
    @GeneratedValue
    @Column(name = "PRODUCT_ID")
    val id: Long? = null
) {}
```

### 양방향

```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @ManyToMany
    @JoinTable(
        name="MEMBER_PRODUCT",
        joinColumns = [JoinColumn(name = "MEMBER_ID")],
        inverseJoinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    val products: MutableList<Product> = arrayListOf()
) {}

@Entity
class Product(
    @Id
    @GeneratedValue
    @Column(name = "PRODUCT_ID")
    val id: Long? = null,
    @ManyToMany(mappedBy = "products")
    val members: MutableList<Member> = arrayListOf()
) {}
```

### 연결 엔티티 사용
```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @OneToMany(mappedBy = "member")
    val products: MutableList<MemberProduct> = arrayListOf()
) {}

@Entity
class Product(
    @Id
    @GeneratedValue
    @Column(name = "PRODUCT_ID")
    val id: Long? = null,
) {}

@Entity
@IdClass(MemberProductId::class)
class MemberProduct(
    @Id
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    val member: Member,
    @Id
    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    val product: Product,
) {}

class MemberProductId (
    val member: Long,
    val product: Long,
) : Serializable {
    // hashCode and equals
} 
```
* 복합 기본키는 별도의 식별자 클래스로 만들어야 한다.
* `Serializable` 을 구현해야한다.
* `equals` 와 `hashCode` 메소드를 구현해야한다.
* 기본 생성자가 있어야한다.
* 식별자 클래스는 public 이어야 한다.
* `@IdClass` 를 이용하는 방법 외에, `@EmbeddedId` 를 사용하는 방법도 존재

### 새로운 기본키 사용
```kotlin
@Entity
class Member(
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    val id: Long? = null,
    @OneToMany(mappedBy = "member")
    val products: MutableList<Order> = arrayListOf()
) {}

@Entity
class Product(
    @Id
    @GeneratedValue
    @Column(name = "PRODUCT_ID")
    val id: Long? = null,
) {}

@Entity
class Order(
    @Id
    @GeneratedValue
    @Column(name = "ORDER_ID")
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    val member: Member,
    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    val product: Product,
) {}
```
* 새로운 기본 키를 활용해서 다대다 관계를 풀어냄